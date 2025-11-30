package org.igdevx.spring_boot_account_microservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.igdevx.spring_boot_account_microservice.dto.KeycloakIdResponse;
import org.igdevx.spring_boot_account_microservice.dto.UserProfileResponse;
import org.igdevx.spring_boot_account_microservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Internal API endpoints for service-to-service communication")
public class InternalController {

    private final UserService userService;

    public InternalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{keycloakId}")
    @Operation(summary = "Retrieve user info via Keycloak ID", 
               description = "Internal endpoint to retrieve user information by Keycloak ID for inter-service communication. Creates user if not exists.")
    public ResponseEntity<UserProfileResponse> getUserByKeycloakId(
            @PathVariable @Parameter(description = "Keycloak user ID (UUID format)") String keycloakId) {
        
        UUID uuid;
        try {
            uuid = UUID.fromString(keycloakId);
        } catch (IllegalArgumentException e) {
            throw new org.igdevx.spring_boot_account_microservice.exception.BadRequestException(
                "Invalid UUID format for keycloakId: " + keycloakId
            );
        }
        
        // Use service method that handles race conditions properly
        UserProfileResponse profile = userService.getOrCreateUserProfile(uuid);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/user/{userId}/keycloak-id")
    @Operation(summary = "Retrieve Keycloak ID by user ID",
               description = "Internal endpoint to retrieve Keycloak ID from user ID for inter-service communication.")
    public ResponseEntity<KeycloakIdResponse> getKeycloakIdByUserId(
            @PathVariable @Parameter(description = "User ID") Long userId) {

        UUID keycloakId = userService.getKeycloakIdByUserId(userId);

        KeycloakIdResponse response = KeycloakIdResponse.builder()
                .userId(userId)
                .keycloakId(keycloakId.toString())
                .build();

        return ResponseEntity.ok(response);
    }
}
