package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RestaurantPublicProfileResponse extends BasePublicProfileResponse {
    private String serviceType;
    private String cuisineType;
    private String hygieneCertifications;
    private String awards;
}
