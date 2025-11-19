package org.igdevx.spring_boot_account_microservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.igdevx.spring_boot_account_microservice.dto.StripeConnectedAccountRequest;
import org.igdevx.spring_boot_account_microservice.dto.StripeConnectedAccountResponse;
import org.igdevx.spring_boot_account_microservice.dto.UserProfileResponse;
import org.igdevx.spring_boot_account_microservice.service.StripeConnectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account/stripe")
@Tag(name = "Stripe Connect", description = "Stripe Connect integration for connected accounts")
public class StripeConnectController {

    private final StripeConnectService stripeConnectService;

    public StripeConnectController(StripeConnectService stripeConnectService) {
        this.stripeConnectService = stripeConnectService;
    }

    @PostMapping("/connected-account")
    @Operation(summary = "Create Stripe connected account", 
               description = "Create a new Stripe Express connected account for the authenticated user")
    public ResponseEntity<StripeConnectedAccountResponse> createConnectedAccount(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody(required = false) StripeConnectedAccountRequest request) {
        
        // Set defaults if request is null or empty
        if (request == null) {
            request = StripeConnectedAccountRequest.builder()
                    .country("FR")
                    .businessType("individual")
                    .build();
        }
        if (request.getCountry() == null) request.setCountry("FR");
        if (request.getBusinessType() == null) request.setBusinessType("individual");
        
        StripeConnectedAccountResponse response = stripeConnectService.createConnectedAccount(
                UUID.fromString(keycloakId), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/connected-account")
    @Operation(summary = "Get Stripe connected account info", 
               description = "Retrieve Stripe connected account information for the authenticated user")
    public ResponseEntity<StripeConnectedAccountResponse> getConnectedAccount(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        
        StripeConnectedAccountResponse response = stripeConnectService.getConnectedAccountInfo(UUID.fromString(keycloakId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-onboarding")
    @Operation(summary = "Refresh onboarding link", 
               description = "Generate a new onboarding link for incomplete Stripe account setup")
    public ResponseEntity<StripeConnectedAccountResponse> refreshOnboardingLink(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        
        StripeConnectedAccountResponse response = stripeConnectService.refreshOnboardingLink(UUID.fromString(keycloakId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync-status")
    @Operation(summary = "Sync account status from Stripe", 
               description = "Manually sync the account status and onboarding completion from Stripe")
    public ResponseEntity<UserProfileResponse> syncAccountStatus(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        
        UserProfileResponse response = stripeConnectService.syncAccountStatus(UUID.fromString(keycloakId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/connected-account")
    @Operation(summary = "Delete Stripe connected account", 
               description = "Remove Stripe connected account from user profile (account remains in Stripe)")
    public ResponseEntity<Void> deleteConnectedAccount(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        
        stripeConnectService.deleteConnectedAccount(UUID.fromString(keycloakId));
        return ResponseEntity.noContent().build();
    }
}