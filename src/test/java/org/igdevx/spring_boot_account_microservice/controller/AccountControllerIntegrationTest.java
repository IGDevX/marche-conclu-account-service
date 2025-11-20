package org.igdevx.spring_boot_account_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.igdevx.spring_boot_account_microservice.dto.*;
import org.igdevx.spring_boot_account_microservice.model.User;
import org.igdevx.spring_boot_account_microservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

        @Autowired
        private org.igdevx.spring_boot_account_microservice.repository.ProfessionRepository professionRepository;

    private String testKeycloakId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
                professionRepository.deleteAll();
        testKeycloakId = UUID.randomUUID().toString();
        
        testUser = User.builder()
                .keycloakId(UUID.fromString(testKeycloakId))
                .biography("Test bio")
                .website("https://test.com")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/api/v1/account/me")
                        .header("X-Keycloak-Id", testKeycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId", is(testKeycloakId)))
                .andExpect(jsonPath("$.biography", is("Test bio")))
                .andExpect(jsonPath("$.website", is("https://test.com")));
    }

    @Test
    void testUpdatePersonalInfo() throws Exception {
        UpdatePersonalInfoRequest request = UpdatePersonalInfoRequest.builder()
                .biography("Updated bio")
                .website("https://updated.com")
                .facebook("https://facebook.com/test")
                .build();

        mockMvc.perform(put("/api/v1/account/me")
                        .header("X-Keycloak-Id", testKeycloakId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.biography", is("Updated bio")))
                .andExpect(jsonPath("$.website", is("https://updated.com")))
                .andExpect(jsonPath("$.facebook", is("https://facebook.com/test")));
    }

    @Test
    void testCreateRestaurantProfile() throws Exception {
        String newKeycloakId = UUID.randomUUID().toString();
        
        RestaurantProfileRequest request = RestaurantProfileRequest.builder()
                .biography("Great restaurant")
                .website("https://restaurant.com")
                .serviceType("Dine-in")
                .cuisineType("Italian")
                .hygieneCertifications("A+")
                .awards("Best Pizza 2023")
                .build();

        mockMvc.perform(post("/api/v1/account/restaurant")
                        .header("X-Keycloak-Id", newKeycloakId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.biography", is("Great restaurant")))
                .andExpect(jsonPath("$.serviceType", is("Dine-in")))
                .andExpect(jsonPath("$.cuisineType", is("Italian")));
    }

    @Test
    void testGetRestaurantProfile() throws Exception {
        // Create a restaurant profile first
        User restaurant = User.builder()
                .keycloakId(UUID.randomUUID())
                .biography("Restaurant bio")
                .serviceType("Dine-in")
                .cuisineType("French")
                .build();
        restaurant = userRepository.save(restaurant);

        mockMvc.perform(get("/api/v1/account/restaurant/" + restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId().intValue())))
                .andExpect(jsonPath("$.biography", is("Restaurant bio")))
                .andExpect(jsonPath("$.serviceType", is("Dine-in")))
                .andExpect(jsonPath("$.cuisineType", is("French")));
    }

    @Test
    void testCreateProducerProfile() throws Exception {
        String newKeycloakId = UUID.randomUUID().toString();
        // create sample professions and use their ids in the request
        org.igdevx.spring_boot_account_microservice.model.Profession prof1 = org.igdevx.spring_boot_account_microservice.model.Profession.builder()
                .code("FARMER")
                .nameEn("Farmer")
                .nameFr("Agriculteur")
                .build();
        prof1 = professionRepository.save(prof1);
        
        org.igdevx.spring_boot_account_microservice.model.Profession prof2 = org.igdevx.spring_boot_account_microservice.model.Profession.builder()
                .code("CHEESEMAKER")
                .nameEn("Cheesemaker")
                .nameFr("Fromager")
                .build();
        prof2 = professionRepository.save(prof2);
        
        ProducerProfileRequest request = ProducerProfileRequest.builder()
                .biography("Organic farmer")
                .website("https://farm.com")
                .siret("12345678901234")
                .organizationType("Family Farm")
                .installationYear(2015)
                .employeesCount(5)
                .professionIds(java.util.Arrays.asList(prof1.getId(), prof2.getId()))
                .build();

        mockMvc.perform(post("/api/v1/account/producer")
                        .header("X-Keycloak-Id", newKeycloakId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.biography", is("Organic farmer")))
                .andExpect(jsonPath("$.siret", is("12345678901234")))
                .andExpect(jsonPath("$.professions", hasSize(2)))
                .andExpect(jsonPath("$.professions[*].code", hasItem("FARMER")))
                .andExpect(jsonPath("$.professions[*].code", hasItem("CHEESEMAKER")));
    }

    @Test
    void testGetProducerProfile() throws Exception {
        // Create a producer profile first
        User producer = User.builder()
                .keycloakId(UUID.randomUUID())
                .biography("Producer bio")
                .siret("98765432109876")
                .build();
        producer = userRepository.save(producer);

        mockMvc.perform(get("/api/v1/account/producer/" + producer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(producer.getId().intValue())))
                .andExpect(jsonPath("$.biography", is("Producer bio")))
                .andExpect(jsonPath("$.siret", is("98765432109876")));
    }

    @Test
    void testDeleteRestaurantProfile() throws Exception {
        User restaurant = User.builder()
                .keycloakId(UUID.randomUUID())
                .serviceType("Dine-in")
                .cuisineType("French")
                .build();
        restaurant = userRepository.save(restaurant);

        mockMvc.perform(delete("/api/v1/account/restaurant/" + restaurant.getId()))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/v1/account/restaurant/" + restaurant.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProducerProfile() throws Exception {
        User producer = User.builder()
                .keycloakId(UUID.randomUUID())
                .siret("12345678901234")
                .build();
        producer = userRepository.save(producer);

        mockMvc.perform(delete("/api/v1/account/producer/" + producer.getId()))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/v1/account/producer/" + producer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserByKeycloakId_Internal() throws Exception {
        mockMvc.perform(get("/api/v1/internal/" + testKeycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId", is(testKeycloakId)))
                .andExpect(jsonPath("$.biography", is("Test bio")));
    }

    @Test
    void testGetNonExistentUser() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        
        mockMvc.perform(get("/api/v1/account/me")
                        .header("X-Keycloak-Id", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    void testGetRestaurantProfile_NotARestaurant() throws Exception {
        // User with no restaurant fields
        User user = User.builder()
                .keycloakId(UUID.randomUUID())
                .biography("Just a bio")
                .build();
        user = userRepository.save(user);

        mockMvc.perform(get("/api/v1/account/restaurant/" + user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("is not a restaurant")));
    }
}
