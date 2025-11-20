package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String keycloakId;
    private String biography;
    private String website;
    private String facebook;
    private String instagram;
    private String linkedin;
    
    // Producer-specific fields
    private String siret;
    private String organizationType;
    private Integer installationYear;
    private Integer employeesCount;
    private List<ProfessionDto> professions;
    
    // Restaurant-specific fields
    private String serviceType;
    private String cuisineType;
    private String hygieneCertifications;
    private String awards;
    
    // Stripe Connect fields
    private String stripeAccountId;
    private String stripeAccountStatus;
    private Boolean stripeOnboardingComplete;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
