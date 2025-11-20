package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BasePublicProfileResponse {
    private Long id;
    private String biography;
    private String website;
    private String facebook;
    private String instagram;
    private String linkedin;
}
