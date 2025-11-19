package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeConnectedAccountResponse {
    private String stripeAccountId;
    private String onboardingUrl;
    private Boolean onboardingComplete;
    private String accountStatus; // pending_onboarding, pending_verification, active, etc.
    private String dashboardUrl;
}