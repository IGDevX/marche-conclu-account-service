package org.igdevx.spring_boot_account_microservice.controller;

import org.igdevx.spring_boot_account_microservice.model.User;
import org.igdevx.spring_boot_account_microservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/keycloak-notification")
    public ResponseEntity<?> handleKeycloakNotification(@RequestBody KeycloakNotificationRequest request) {
        User user = new User();
        user.setKeycloakId(UUID.fromString(request.getKeycloakId()));
        userService.saveUser(user);
        return ResponseEntity.ok("User created successfully");
    }
}

class KeycloakNotificationRequest {
    private String keycloakId;

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
}