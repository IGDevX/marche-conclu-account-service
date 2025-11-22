package org.igdevx.spring_boot_account_microservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    // User creation is now handled by InternalController.getUserByKeycloakId()
    // which uses getOrCreateUserProfile() with proper race condition handling
    
    // Additional user-related endpoints can be added here in the future
}