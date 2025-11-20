package org.igdevx.spring_boot_account_microservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.igdevx.spring_boot_account_microservice.dto.*;
import org.igdevx.spring_boot_account_microservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
@Tag(name = "Account", description = "Account management endpoints")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // ===== Personal Account Endpoints =====

    @GetMapping("/me")
    @Operation(summary = "Get connected user's full profile", description = "Retrieve the full profile of the currently authenticated user")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        UserProfileResponse profile = userService.getUserProfile(UUID.fromString(keycloakId));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(summary = "Update personal information", description = "Update the personal information of the currently authenticated user")
    public ResponseEntity<UserProfileResponse> updatePersonalInfo(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody UpdatePersonalInfoRequest request) {
        UserProfileResponse profile = userService.updatePersonalInfo(UUID.fromString(keycloakId), request);
        return ResponseEntity.ok(profile);
    }

    // ===== Restaurant Endpoints =====

    @GetMapping("/restaurant/{id}")
    @Operation(summary = "Get restaurant owner's public profile", description = "Retrieve the public profile of a restaurant by ID")
    public ResponseEntity<RestaurantPublicProfileResponse> getRestaurantProfile(
            @PathVariable @Parameter(description = "Restaurant user ID") Long id) {
        RestaurantPublicProfileResponse profile = userService.getRestaurantProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/restaurant")
    @Operation(summary = "Create/complete restaurant profile", description = "Create or complete a restaurant profile for the authenticated user")
    public ResponseEntity<UserProfileResponse> createRestaurantProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody RestaurantProfileRequest request) {
        UserProfileResponse profile = userService.createOrUpdateRestaurantProfile(UUID.fromString(keycloakId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PutMapping("/restaurant")
    @Operation(summary = "Update my restaurant profile", description = "Update the authenticated user's restaurant profile")
    public ResponseEntity<UserProfileResponse> updateMyRestaurantProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody RestaurantProfileRequest request) {
        UserProfileResponse profile = userService.createOrUpdateRestaurantProfile(UUID.fromString(keycloakId), request);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/restaurant")
    @Operation(summary = "Delete my restaurant profile", description = "Delete the authenticated user's restaurant profile")
    public ResponseEntity<Void> deleteMyRestaurantProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        userService.deleteRestaurantProfileByKeycloakId(UUID.fromString(keycloakId));
        return ResponseEntity.noContent().build();
    }

    // ===== Producer Endpoints =====

    @GetMapping("/producer/{id}")
    @Operation(summary = "Get producer's public profile", description = "Retrieve the public profile of a producer by ID")
    public ResponseEntity<ProducerPublicProfileResponse> getProducerProfile(
            @PathVariable @Parameter(description = "Producer user ID") Long id) {
        ProducerPublicProfileResponse profile = userService.getProducerProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/producer")
    @Operation(summary = "Create/complete producer profile", description = "Create or complete a producer profile for the authenticated user")
    public ResponseEntity<UserProfileResponse> createProducerProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody ProducerProfileRequest request) {
        UserProfileResponse profile = userService.createOrUpdateProducerProfile(UUID.fromString(keycloakId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PutMapping("/producer")
    @Operation(summary = "Update my producer profile", description = "Update the authenticated user's producer profile")
    public ResponseEntity<UserProfileResponse> updateMyProducerProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @RequestBody ProducerProfileRequest request) {
        UserProfileResponse profile = userService.createOrUpdateProducerProfile(UUID.fromString(keycloakId), request);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/producer")
    @Operation(summary = "Delete my producer profile", description = "Delete the authenticated user's producer profile")
    public ResponseEntity<Void> deleteMyProducerProfile(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId) {
        userService.deleteProducerProfileByKeycloakId(UUID.fromString(keycloakId));
        return ResponseEntity.noContent().build();
    }

    // ===== Producer Professions Management =====

    @PostMapping("/producer/professions/{professionId}")
    @Operation(summary = "Add profession to producer", description = "Add a profession to the authenticated producer's profile")
    public ResponseEntity<UserProfileResponse> addProfession(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @PathVariable @Parameter(description = "Profession ID to add") Long professionId) {
        UserProfileResponse profile = userService.addProfessionToProducer(UUID.fromString(keycloakId), professionId);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/producer/professions/{professionId}")
    @Operation(summary = "Remove profession from producer", description = "Remove a profession from the authenticated producer's profile")
    public ResponseEntity<UserProfileResponse> removeProfession(
            @RequestHeader("X-Keycloak-Id") @Parameter(description = "Keycloak user ID from authentication") String keycloakId,
            @PathVariable @Parameter(description = "Profession ID to remove") Long professionId) {
        UserProfileResponse profile = userService.removeProfessionFromProducer(UUID.fromString(keycloakId), professionId);
        return ResponseEntity.ok(profile);
    }
}
