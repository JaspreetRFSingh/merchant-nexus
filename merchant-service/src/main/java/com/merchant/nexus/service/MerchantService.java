package com.merchant.nexus.service;

import com.merchant.nexus.dto.MerchantDTO;
import com.merchant.nexus.dto.MerchantDTO.CreateMerchantRequest;
import com.merchant.nexus.dto.MerchantDTO.MerchantResponse;
import com.merchant.nexus.exception.BusinessException;
import com.merchant.nexus.exception.ResourceNotFoundException;
import com.merchant.nexus.exception.ValidationException;
import com.merchant.nexus.model.Merchant;
import com.merchant.nexus.repository.MerchantEntity;
import com.merchant.nexus.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Merchant operations - demonstrates transaction management, business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantEventPublisher eventPublisher;

    @Transactional
    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        // Validate unique constraints
        if (merchantRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("email", "Email already registered");
        }
        if (merchantRepository.findByBusinessRegistrationNumber(
                request.getBusinessRegistrationNumber()).isPresent()) {
            throw new ValidationException("businessRegistrationNumber", 
                "Business registration number already exists");
        }

        // Create merchant domain object
        Merchant.Address address = null;
        if (request.getAddress() != null) {
            address = Merchant.Address.builder()
                    .street(request.getAddress().getStreet())
                    .city(request.getAddress().getCity())
                    .state(request.getAddress().getState())
                    .postalCode(request.getAddress().getPostalCode())
                    .country(request.getAddress().getCountry())
                    .build();
        }

        Merchant merchant = Merchant.createNew(
                request.getBusinessName(),
                request.getBusinessRegistrationNumber(),
                request.getOwnerName(),
                request.getEmail(),
                request.getPhone()
        );
        
        // Update address via builder pattern workaround
        Merchant merchantWithAddress = Merchant.builder()
                .id(merchant.getId())
                .businessName(merchant.getBusinessName())
                .businessRegistrationNumber(merchant.getBusinessRegistrationNumber())
                .ownerName(merchant.getOwnerName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .status(merchant.getStatus())
                .address(address)
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();

        // Save to database
        MerchantEntity savedEntity = merchantRepository.save(MerchantEntity.fromDomain(merchantWithAddress));

        // Publish domain event
        eventPublisher.publishMerchantCreated(savedEntity.toDomain());

        log.info("Created merchant with id: {}", savedEntity.getId());
        return MerchantDTO.toResponse(savedEntity.toDomain());
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(String id) {
        MerchantEntity entity = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", id));
        return MerchantDTO.toResponse(entity.toDomain());
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllMerchants(Pageable pageable) {
        Page<MerchantEntity> entityPage = merchantRepository.findAll(pageable);
        return entityPage.stream()
                .map(MerchantEntity::toDomain)
                .map(MerchantDTO::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getMerchantsByStatus(Merchant.MerchantStatus status, 
                                                        Pageable pageable) {
        Page<MerchantEntity> entityPage = merchantRepository.findByStatus(status, pageable);
        return entityPage.stream()
                .map(MerchantEntity::toDomain)
                .map(MerchantDTO::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MerchantResponse verifyMerchant(String id, String verifiedBy) {
        MerchantEntity entity = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", id));

        if (entity.getStatus() != Merchant.MerchantStatus.PENDING_VERIFICATION) {
            throw new BusinessException("Merchant is not in pending verification status");
        }

        entity.setStatus(Merchant.MerchantStatus.ACTIVE);
        MerchantEntity savedEntity = merchantRepository.save(entity);

        eventPublisher.publishMerchantVerified(savedEntity.toDomain(), verifiedBy);

        log.info("Verified merchant with id: {}", id);
        return MerchantDTO.toResponse(savedEntity.toDomain());
    }

    @Transactional
    public MerchantResponse suspendMerchant(String id, String reason, String suspendedBy) {
        MerchantEntity entity = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", id));

        entity.setStatus(Merchant.MerchantStatus.SUSPENDED);
        MerchantEntity savedEntity = merchantRepository.save(entity);

        eventPublisher.publishMerchantSuspended(savedEntity.toDomain(), reason, suspendedBy);

        log.info("Suspended merchant with id: {}", id);
        return MerchantDTO.toResponse(savedEntity.toDomain());
    }

    @Transactional
    public MerchantResponse activateMerchant(String id, String activatedBy) {
        MerchantEntity entity = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", id));

        if (entity.getStatus() != Merchant.MerchantStatus.SUSPENDED) {
            throw new BusinessException("Merchant is not suspended");
        }

        entity.setStatus(Merchant.MerchantStatus.ACTIVE);
        MerchantEntity savedEntity = merchantRepository.save(entity);

        eventPublisher.publishMerchantActivated(savedEntity.toDomain(), activatedBy);

        log.info("Activated merchant with id: {}", id);
        return MerchantDTO.toResponse(savedEntity.toDomain());
    }

    @Transactional
    public MerchantResponse deactivateMerchant(String id, String reason, String deactivatedBy) {
        MerchantEntity entity = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", id));

        entity.setStatus(Merchant.MerchantStatus.DEACTIVATED);
        MerchantEntity savedEntity = merchantRepository.save(entity);

        eventPublisher.publishMerchantDeactivated(savedEntity.toDomain(), reason, deactivatedBy);

        log.info("Deactivated merchant with id: {}", id);
        return MerchantDTO.toResponse(savedEntity.toDomain());
    }

    @Transactional(readOnly = true)
    public long countByStatus(Merchant.MerchantStatus status) {
        return merchantRepository.countByStatus(status);
    }
}
