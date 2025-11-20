package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeConnectedAccountRequest {
    private String country; // Default: "FR"
    private String businessType; // "individual" or "company" 
    private String email; // Optional - will use Keycloak ID as fallback
}