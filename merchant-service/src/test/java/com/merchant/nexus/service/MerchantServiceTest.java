package com.merchant.nexus.service;

import com.merchant.nexus.dto.MerchantDTO;
import com.merchant.nexus.exception.ResourceNotFoundException;
import com.merchant.nexus.exception.ValidationException;
import com.merchant.nexus.model.Merchant;
import com.merchant.nexus.repository.MerchantEntity;
import com.merchant.nexus.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for MerchantService.
 * Demonstrates: Unit testing patterns, Mockito, AssertJ assertions
 */
@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantEventPublisher eventPublisher;

    @InjectMocks
    private MerchantService merchantService;

    private MerchantEntity testMerchantEntity;
    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        testMerchantEntity = new MerchantEntity();
        testMerchantEntity.setId("test-merchant-id");
        testMerchantEntity.setBusinessName("Test Merchant");
        testMerchantEntity.setEmail("test@merchant.com");
        testMerchantEntity.setPhone("+82-10-1234-5678");
        testMerchantEntity.setBusinessRegistrationNumber("123-45-67890");
        testMerchantEntity.setOwnerName("Test Owner");
        testMerchantEntity.setStatus(Merchant.MerchantStatus.ACTIVE);

        testMerchant = testMerchantEntity.toDomain();
    }

    @Test
    @DisplayName("Should create a new merchant successfully")
    void createMerchant_Success() {
        // Given
        MerchantDTO.CreateMerchantRequest request = MerchantDTO.CreateMerchantRequest.builder()
                .businessName("New Merchant")
                .businessRegistrationNumber("987-65-43210")
                .ownerName("New Owner")
                .email("new@merchant.com")
                .phone("+82-10-9876-5432")
                .build();

        given(merchantRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(merchantRepository.findByBusinessRegistrationNumber(request.getBusinessRegistrationNumber()))
                .willReturn(Optional.empty());
        given(merchantRepository.save(any(MerchantEntity.class))).willReturn(testMerchantEntity);

        // When
        var result = merchantService.createMerchant(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBusinessName()).isEqualTo("Test Merchant");
        
        ArgumentCaptor<MerchantEntity> entityCaptor = ArgumentCaptor.forClass(MerchantEntity.class);
        verify(merchantRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getBusinessName()).isEqualTo("New Merchant");
        
        verify(eventPublisher).publishMerchantCreated(any(Merchant.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when email already exists")
    void createMerchant_EmailExists_ThrowsValidationException() {
        // Given
        MerchantDTO.CreateMerchantRequest request = MerchantDTO.CreateMerchantRequest.builder()
                .businessName("New Merchant")
                .email("existing@merchant.com")
                .build();

        given(merchantRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.of(testMerchantEntity));

        // When & Then
        assertThatThrownBy(() -> merchantService.createMerchant(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already registered");
        
        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get merchant by ID successfully")
    void getMerchantById_Success() {
        // Given
        given(merchantRepository.findById("test-merchant-id")).willReturn(Optional.of(testMerchantEntity));

        // When
        var result = merchantService.getMerchantById("test-merchant-id");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-merchant-id");
        assertThat(result.getBusinessName()).isEqualTo("Test Merchant");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when merchant not found")
    void getMerchantById_NotFound_ThrowsException() {
        // Given
        given(merchantRepository.findById("non-existent-id")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> merchantService.getMerchantById("non-existent-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found with id: non-existent-id");
    }

    @Test
    @DisplayName("Should verify merchant successfully")
    void verifyMerchant_Success() {
        // Given
        testMerchantEntity.setStatus(Merchant.MerchantStatus.PENDING_VERIFICATION);
        given(merchantRepository.findById("test-merchant-id")).willReturn(Optional.of(testMerchantEntity));
        given(merchantRepository.save(any(MerchantEntity.class))).willReturn(testMerchantEntity);

        // When
        var result = merchantService.verifyMerchant("test-merchant-id", "admin-user");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(eventPublisher).publishMerchantVerified(any(Merchant.class), eq("admin-user"));
    }

    @Test
    @DisplayName("Should throw BusinessException when verifying non-pending merchant")
    void verifyMerchant_NotPending_ThrowsBusinessException() {
        // Given - merchant is already ACTIVE
        testMerchantEntity.setStatus(Merchant.MerchantStatus.ACTIVE);
        given(merchantRepository.findById("test-merchant-id")).willReturn(Optional.of(testMerchantEntity));

        // When & Then
        assertThatThrownBy(() -> merchantService.verifyMerchant("test-merchant-id", "admin-user"))
                .isInstanceOf(com.coupang.merchant.exception.BusinessException.class)
                .hasMessageContaining("not in pending verification status");
    }

    @Test
    @DisplayName("Should suspend merchant successfully")
    void suspendMerchant_Success() {
        // Given
        given(merchantRepository.findById("test-merchant-id")).willReturn(Optional.of(testMerchantEntity));
        given(merchantRepository.save(any(MerchantEntity.class))).willReturn(testMerchantEntity);

        // When
        var result = merchantService.suspendMerchant("test-merchant-id", "Policy violation", "admin-user");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SUSPENDED");
        verify(eventPublisher).publishMerchantSuspended(
                any(Merchant.class), 
                eq("Policy violation"), 
                eq("admin-user")
        );
    }

    @Test
    @DisplayName("Should count merchants by status")
    void countByStatus_Success() {
        // Given
        given(merchantRepository.countByStatus(Merchant.MerchantStatus.ACTIVE)).willReturn(42L);

        // When
        long count = merchantService.countByStatus(Merchant.MerchantStatus.ACTIVE);

        // Then
        assertThat(count).isEqualTo(42L);
        verify(merchantRepository).countByStatus(Merchant.MerchantStatus.ACTIVE);
    }
}
