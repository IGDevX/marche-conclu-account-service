package org.igdevx.spring_boot_account_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakIdResponse {
    private Long userId;
    private String keycloakId;
}

