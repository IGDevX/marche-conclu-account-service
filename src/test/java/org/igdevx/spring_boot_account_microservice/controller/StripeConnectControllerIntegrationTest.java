package org.igdevx.spring_boot_account_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.igdevx.spring_boot_account_microservice.dto.StripeConnectedAccountRequest;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // Use test profile to avoid hitting real Stripe API
public class StripeConnectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String testKeycloakId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testKeycloakId = UUID.randomUUID().toString();
        
        testUser = User.builder()
                .keycloakId(UUID.fromString(testKeycloakId))
                .biography("Test producer")
                .siret("12345678901234")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateConnectedAccount_UserNotFound() throws Exception {
        String nonExistentKeycloakId = UUID.randomUUID().toString();
        
        StripeConnectedAccountRequest request = StripeConnectedAccountRequest.builder()
                .country("FR")
                .businessType("individual")
                .build();

        mockMvc.perform(post("/api/v1/account/stripe/connected-account")
                        .header("X-Keycloak-Id", nonExistentKeycloakId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test 
    void testGetConnectedAccount_NoStripeAccount() throws Exception {
        mockMvc.perform(get("/api/v1/account/stripe/connected-account")
                        .header("X-Keycloak-Id", testKeycloakId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not have a Stripe connected account")));
    }

    @Test
    void testDeleteConnectedAccount_NoStripeAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/account/stripe/connected-account")
                        .header("X-Keycloak-Id", testKeycloakId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not have a Stripe connected account")));
    }

    @Test
    void testRefreshOnboardingLink_NoStripeAccount() throws Exception {
        mockMvc.perform(post("/api/v1/account/stripe/refresh-onboarding")
                        .header("X-Keycloak-Id", testKeycloakId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not have a Stripe connected account")));
    }

    @Test
    void testSyncAccountStatus_NoStripeAccount() throws Exception {
        mockMvc.perform(post("/api/v1/account/stripe/sync-status")
                        .header("X-Keycloak-Id", testKeycloakId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not have a Stripe connected account")));
    }

    @Test
    void testCreateConnectedAccountWithDefaults() throws Exception {
        // Test with empty request body - should use defaults
        mockMvc.perform(post("/api/v1/account/stripe/connected-account")
                        .header("X-Keycloak-Id", testKeycloakId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stripeAccountId").exists())
                .andExpect(jsonPath("$.onboardingUrl").exists())
                .andExpect(jsonPath("$.onboardingComplete", is(false)))
                .andExpect(jsonPath("$.accountStatus", is("pending_onboarding")));
    }

    // Note: The actual Stripe API calls would be mocked in a real test environment
    // For integration testing with real Stripe API, you would need:
    // 1. Test API keys
    // 2. Proper test configuration
    // 3. Cleanup of test accounts after tests
}