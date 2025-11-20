package org.igdevx.spring_boot_account_microservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import org.igdevx.spring_boot_account_microservice.dto.StripeConnectedAccountRequest;
import org.igdevx.spring_boot_account_microservice.dto.StripeConnectedAccountResponse;
import org.igdevx.spring_boot_account_microservice.dto.UserProfileResponse;
import org.igdevx.spring_boot_account_microservice.exception.BadRequestException;
import org.igdevx.spring_boot_account_microservice.exception.ResourceNotFoundException;
import org.igdevx.spring_boot_account_microservice.model.User;
import org.igdevx.spring_boot_account_microservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StripeConnectService {

    private static final Logger log = LoggerFactory.getLogger(StripeConnectService.class);

    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final UserService userService;

    public StripeConnectService(StripeService stripeService, UserRepository userRepository, UserService userService) {
        this.stripeService = stripeService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Create a new Stripe connected account for a user
     */
    public StripeConnectedAccountResponse createConnectedAccount(UUID keycloakId, StripeConnectedAccountRequest request) {
        // Get or create user first
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));

        // Check if user already has a Stripe account
        if (user.getStripeAccountId() != null) {
            log.info("User {} already has Stripe account {}, returning existing account info", 
                    user.getId(), user.getStripeAccountId());
            return getConnectedAccountInfo(keycloakId);
        }

        try {
            // Create Stripe account
            Account stripeAccount = stripeService.createConnectedAccount(user);
            
            // Update user with Stripe account ID
            user.setStripeAccountId(stripeAccount.getId());
            user.setStripeAccountStatus("pending_onboarding");
            user.setStripeOnboardingComplete(false);
            userRepository.save(user);

            // Create onboarding link
            AccountLink accountLink = stripeService.createAccountLink(stripeAccount.getId());

            log.info("Created Stripe connected account {} for user {}", stripeAccount.getId(), user.getId());

            return StripeConnectedAccountResponse.builder()
                    .stripeAccountId(stripeAccount.getId())
                    .onboardingUrl(accountLink.getUrl())
                    .onboardingComplete(false)
                    .accountStatus("pending_onboarding")
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create Stripe connected account for user {}: {}", user.getId(), e.getMessage());
            throw new BadRequestException("Failed to create Stripe account: " + e.getMessage());
        }
    }

    /**
     * Get connected account information for a user
     */
    public StripeConnectedAccountResponse getConnectedAccountInfo(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getStripeAccountId() == null) {
            throw new BadRequestException("User does not have a Stripe connected account");
        }

        try {
            // Sync status from Stripe
            syncStripeAccountStatus(user);
            
            // Generate dashboard URL for active accounts
            String dashboardUrl = null;
            if ("active".equals(user.getStripeAccountStatus())) {
                dashboardUrl = "https://dashboard.stripe.com/express/" + user.getStripeAccountId();
            }

            return StripeConnectedAccountResponse.builder()
                    .stripeAccountId(user.getStripeAccountId())
                    .onboardingUrl(null) // Don't return onboarding URL for existing accounts
                    .onboardingComplete(user.getStripeOnboardingComplete())
                    .accountStatus(user.getStripeAccountStatus())
                    .dashboardUrl(dashboardUrl)
                    .build();

        } catch (Exception e) {
            log.warn("Could not sync Stripe account status for user {}: {}", user.getId(), e.getMessage());
            
            // Return cached status if Stripe is unavailable
            return StripeConnectedAccountResponse.builder()
                    .stripeAccountId(user.getStripeAccountId())
                    .onboardingComplete(user.getStripeOnboardingComplete())
                    .accountStatus(user.getStripeAccountStatus() != null ? user.getStripeAccountStatus() : "unknown")
                    .build();
        }
    }

    /**
     * Refresh onboarding link for incomplete accounts
     */
    public StripeConnectedAccountResponse refreshOnboardingLink(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getStripeAccountId() == null) {
            throw new BadRequestException("User does not have a Stripe connected account");
        }

        if (Boolean.TRUE.equals(user.getStripeOnboardingComplete())) {
            throw new BadRequestException("User has already completed onboarding");
        }

        try {
            AccountLink accountLink = stripeService.createAccountLink(user.getStripeAccountId());
            
            return StripeConnectedAccountResponse.builder()
                    .stripeAccountId(user.getStripeAccountId())
                    .onboardingUrl(accountLink.getUrl())
                    .onboardingComplete(user.getStripeOnboardingComplete())
                    .accountStatus(user.getStripeAccountStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create onboarding link for Stripe account {}: {}", user.getStripeAccountId(), e.getMessage());
            throw new BadRequestException("Failed to create onboarding link: " + e.getMessage());
        }
    }

    /**
     * Sync account status from Stripe
     */
    public UserProfileResponse syncAccountStatus(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getStripeAccountId() == null) {
            throw new BadRequestException("User does not have a Stripe connected account");
        }

        syncStripeAccountStatus(user);
        
        // Return updated user profile
        return userService.getUserProfile(keycloakId);
    }

    /**
     * Delete connected account from user profile
     */
    public void deleteConnectedAccount(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getStripeAccountId() == null) {
            throw new BadRequestException("User does not have a Stripe connected account");
        }

        log.info("Removing Stripe account {} from user {}", user.getStripeAccountId(), user.getId());
        
        // Remove Stripe account reference from user (account remains in Stripe)
        user.setStripeAccountId(null);
        user.setStripeAccountStatus(null);
        user.setStripeOnboardingComplete(null);
        userRepository.save(user);
    }

    /**
     * Internal method to sync account status from Stripe
     */
    private void syncStripeAccountStatus(User user) {
        try {
            boolean isComplete = stripeService.isOnboardingComplete(user.getStripeAccountId());
            String status = stripeService.getAccountStatus(user.getStripeAccountId());
            
            user.setStripeOnboardingComplete(isComplete);
            user.setStripeAccountStatus(status);
            userRepository.save(user);
            
            log.debug("Synced Stripe account status for user {}: complete={}, status={}", 
                    user.getId(), isComplete, status);
                    
        } catch (Exception e) {
            log.warn("Failed to sync Stripe account status for user {}: {}", user.getId(), e.getMessage());
        }
    }
}