package org.igdevx.spring_boot_account_microservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.AccountRetrieveParams;
import org.igdevx.spring_boot_account_microservice.exception.BadRequestException;
import org.igdevx.spring_boot_account_microservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.connect.redirect-url}")
    private String connectRedirectUrl;

    @Value("${stripe.connect.refresh-url}")
    private String connectRefreshUrl;

    /**
     * Create a Stripe Express connected account for a user
     */
    public Account createConnectedAccount(User user) throws StripeException {
        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("FR") // France - adjust based on your market
                    .setEmail(user.getKeycloakId() + "@temp.local") // You might want to get actual email from Keycloak
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(
                                            AccountCreateParams.Capabilities.CardPayments.builder()
                                                    .setRequested(true)
                                                    .build()
                                    )
                                    .setTransfers(
                                            AccountCreateParams.Capabilities.Transfers.builder()
                                                    .setRequested(true)
                                                    .build()
                                    )
                                    .build()
                    )
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL) // or COMPANY based on user type
                    .setMetadata(
                            java.util.Map.of(
                                    "user_id", user.getId().toString(),
                                    "keycloak_id", user.getKeycloakId().toString(),
                                    "user_type", determineUserType(user)
                            )
                    )
                    .build();

            Account account = Account.create(params);
            log.info("Created Stripe connected account {} for user {}", account.getId(), user.getId());
            
            return account;
        } catch (StripeException e) {
            log.error("Failed to create Stripe connected account for user {}: {}", user.getId(), e.getMessage());
            throw new BadRequestException("Failed to create Stripe account: " + e.getMessage());
        }
    }

    /**
     * Create an account link for onboarding
     */
    public AccountLink createAccountLink(String stripeAccountId) throws StripeException {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setRefreshUrl(connectRefreshUrl)
                    .setReturnUrl(connectRedirectUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink accountLink = AccountLink.create(params);
            log.info("Created account link for Stripe account {}", stripeAccountId);
            
            return accountLink;
        } catch (StripeException e) {
            log.error("Failed to create account link for Stripe account {}: {}", stripeAccountId, e.getMessage());
            throw new BadRequestException("Failed to create account link: " + e.getMessage());
        }
    }

    /**
     * Retrieve account information from Stripe
     */
    public Account retrieveAccount(String stripeAccountId) throws StripeException {
        try {
            AccountRetrieveParams params = AccountRetrieveParams.builder().build();
            Account account = Account.retrieve(stripeAccountId, params, null);
            
            log.debug("Retrieved Stripe account {}", stripeAccountId);
            return account;
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe account {}: {}", stripeAccountId, e.getMessage());
            throw new BadRequestException("Failed to retrieve Stripe account: " + e.getMessage());
        }
    }

    /**
     * Check if account onboarding is complete
     * An account is considered complete when details_submitted is true and charges_enabled is true
     */
    public boolean isOnboardingComplete(String stripeAccountId) {
        try {
            Account account = retrieveAccount(stripeAccountId);
            // Check details_submitted first (user completed the onboarding flow)
            // Then check charges_enabled (Stripe verified and enabled charging)
            return account.getDetailsSubmitted() && account.getChargesEnabled();
        } catch (Exception e) {
            log.warn("Could not check onboarding status for account {}: {}", stripeAccountId, e.getMessage());
            return false;
        }
    }

    /**
     * Get account status from Stripe
     * Returns: incomplete, pending, active, rejected
     */
    public String getAccountStatus(String stripeAccountId) {
        try {
            Account account = retrieveAccount(stripeAccountId);
            
            // Check if account is rejected
            if (account.getRequirements() != null && 
                account.getRequirements().getDisabledReason() != null) {
                return "rejected";
            }
            
            // Check if fully active (details submitted and charges enabled)
            if (account.getDetailsSubmitted() && account.getChargesEnabled()) {
                return "active";
            }
            
            // Check if details submitted but not yet verified
            if (account.getDetailsSubmitted()) {
                return "pending";
            }
            
            // User hasn't completed onboarding yet
            return "incomplete";
        } catch (Exception e) {
            log.warn("Could not get account status for {}: {}", stripeAccountId, e.getMessage());
            return "unknown";
        }
    }

    /**
     * Determine user type for Stripe metadata
     */
    private String determineUserType(User user) {
        if (user.getSiret() != null) {
            return "producer";
        } else if (user.getServiceType() != null) {
            return "restaurant";
        } else {
            return "consumer";
        }
    }
}