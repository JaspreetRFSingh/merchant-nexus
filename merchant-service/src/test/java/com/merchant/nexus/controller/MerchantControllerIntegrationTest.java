package com.merchant.nexus.controller;

import com.merchant.nexus.dto.MerchantDTO;
import com.merchant.nexus.model.Merchant;
import com.merchant.nexus.repository.MerchantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for MerchantController.
 * Demonstrates: Integration testing, Testcontainers, MockMvc
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class MerchantControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_merchant_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MerchantRepository merchantRepository;

    @BeforeEach
    void setUp() {
        merchantRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create merchant via API")
    void createMerchant_IntegrationTest() throws Exception {
        // Given
        MerchantDTO.CreateMerchantRequest request = MerchantDTO.CreateMerchantRequest.builder()
                .businessName("Integration Test Merchant")
                .businessRegistrationNumber(UUID.randomUUID().toString())
                .ownerName("Test Owner")
                .email("test" + UUID.randomUUID() + "@merchant.com")
                .phone("+82-10-1234-5678")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessName", is("Integration Test Merchant")))
                .andExpect(jsonPath("$.status", is("PENDING_VERIFICATION")))
                .andExpect(jsonPath("$.email", is(request.getEmail())));
    }

    @Test
    @DisplayName("Should return 400 for invalid email format")
    void createMerchant_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        MerchantDTO.CreateMerchantRequest request = MerchantDTO.CreateMerchantRequest.builder()
                .businessName("Test Merchant")
                .businessRegistrationNumber("123-45-67890")
                .ownerName("Test Owner")
                .email("invalid-email")
                .phone("+82-10-1234-5678")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("Should get merchant by ID")
    void getMerchant_IntegrationTest() throws Exception {
        // Given
        Merchant merchant = Merchant.createNew(
                "Test Merchant",
                UUID.randomUUID().toString(),
                "Test Owner",
                "get" + UUID.randomUUID() + "@test.com",
                "+82-10-1234-5678"
        );
        Merchant savedMerchant = merchantRepository.save(
                MerchantEntity.fromDomain(merchant)
        ).toDomain();

        // When & Then
        mockMvc.perform(get("/api/v1/merchants/{id}", savedMerchant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedMerchant.getId())))
                .andExpect(jsonPath("$.businessName", is("Test Merchant")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent merchant")
    void getMerchant_NotFound_Returns404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/merchants/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("Should list merchants with pagination")
    void listMerchants_IntegrationTest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/merchants")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }
}
