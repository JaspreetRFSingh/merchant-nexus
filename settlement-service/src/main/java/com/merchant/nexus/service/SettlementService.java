package com.merchant.nexus.service;

import com.merchant.nexus.dto.SettlementDTO;
import com.merchant.nexus.exception.BusinessException;
import com.merchant.nexus.exception.ResourceNotFoundException;
import com.merchant.nexus.model.Settlement;
import com.merchant.nexus.repository.SettlementEntity;
import com.merchant.nexus.repository.SettlementRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service layer for Settlement operations.
 * Demonstrates: Saga pattern, distributed locking, circuit breaker, retry patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementEventPublisher eventPublisher;
    private final RedissonClient redissonClient;

    /**
     * Create a new settlement request.
     * Demonstrates: Transaction management, domain logic
     */
    @Transactional
    public SettlementDTO.SettlementResponse createSettlement(
            String merchantId,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal grossAmount,
            BigDecimal commissionRate,
            String bankAccountNumber,
            String bankName) {

        // Check for existing pending settlement for the same period
        Pageable pageable = PageRequest.of(0, 1);
        Page<SettlementEntity> existingSettlements = 
                settlementRepository.findByMerchantIdAndStatus(
                        merchantId, 
                        Settlement.SettlementStatus.PENDING, 
                        pageable);

        for (SettlementEntity existing : existingSettlements.getContent()) {
            if (existing.getSettlementPeriodStart().equals(periodStart.toString()) &&
                existing.getSettlementPeriodEnd().equals(periodEnd.toString())) {
                throw new BusinessException(
                        "SETTLEMENT_ALREADY_EXISTS",
                        "A pending settlement already exists for this period");
            }
        }

        Settlement settlement = Settlement.createNew(
                merchantId,
                periodStart,
                periodEnd,
                grossAmount,
                commissionRate,
                bankAccountNumber,
                bankName
        );

        SettlementEntity savedEntity = settlementRepository.save(SettlementEntity.fromDomain(settlement));

        eventPublisher.publishSettlementRequested(savedEntity.toDomain());

        log.info("Created settlement request for merchant {}: {}", merchantId, savedEntity.getId());
        return SettlementDTO.toResponse(savedEntity.toDomain());
    }

    /**
     * Approve a settlement for processing.
     * Demonstrates: Pessimistic locking, state machine
     */
    @Transactional
    public SettlementDTO.SettlementResponse approveSettlement(String settlementId, String approvedBy) {
        // Use pessimistic locking to prevent concurrent modifications
        SettlementEntity entity = settlementRepository.findByIdForUpdate(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if (entity.getStatus() != Settlement.SettlementStatus.PENDING) {
            throw new BusinessException("Settlement is not in pending status");
        }

        entity.setStatus(Settlement.SettlementStatus.PROCESSING);
        entity.setProcessedBy(approvedBy);
        
        SettlementEntity savedEntity = settlementRepository.save(entity);

        eventPublisher.publishSettlementApproved(savedEntity.toDomain(), approvedBy);

        log.info("Approved settlement {} by {}", settlementId, approvedBy);
        return SettlementDTO.toResponse(savedEntity.toDomain());
    }

    /**
     * Process a settlement payment with circuit breaker and retry.
     * Demonstrates: Circuit breaker pattern, retry pattern, saga pattern
     */
    @Transactional
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentProcessing")
    public SettlementDTO.SettlementResponse processSettlement(String settlementId) {
        // Acquire distributed lock using Redis
        RLock lock = redissonClient.getLock("settlement:process:" + settlementId);
        
        try {
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("SETTLEMENT_LOCK_FAILED", 
                        "Could not acquire lock for settlement processing");
            }

            try {
                SettlementEntity entity = settlementRepository.findByIdForUpdate(settlementId)
                        .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

                if (entity.getStatus() != Settlement.SettlementStatus.PROCESSING) {
                    throw new BusinessException("Settlement is not in processing status");
                }

                // Simulate payment gateway call
                String paymentReference = callPaymentGateway(
                        entity.getMerchantId(),
                        entity.getNetAmount(),
                        entity.getBankAccountNumber(),
                        entity.getBankName()
                );

                entity.setPaymentReference(paymentReference);
                entity.setStatus(Settlement.SettlementStatus.COMPLETED);
                
                SettlementEntity savedEntity = settlementRepository.save(entity);

                eventPublisher.publishSettlementCompleted(savedEntity.toDomain(), paymentReference);

                log.info("Completed settlement {} with payment reference {}", settlementId, paymentReference);
                return SettlementDTO.toResponse(savedEntity.toDomain());

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("SETTLEMENT_PROCESSING_INTERRUPTED", 
                    "Settlement processing was interrupted");
        }
    }

    /**
     * Fallback method for circuit breaker.
     */
    public SettlementDTO.SettlementResponse processPaymentFallback(String settlementId, Throwable t) {
        log.error("Payment gateway circuit breaker opened for settlement {}: {}", settlementId, t.getMessage());
        throw new BusinessException("PAYMENT_GATEWAY_UNAVAILABLE", 
                "Payment gateway is temporarily unavailable. Please try again later.");
    }

    /**
     * Simulated payment gateway call.
     * Demonstrates: External service integration
     */
    private String callPaymentGateway(String merchantId, BigDecimal amount, 
                                       String accountNumber, String bankName) {
        // In production, this would call an actual payment gateway API
        log.info("Processing payment for merchant {} amount {} to {} {}", 
                merchantId, amount, bankName, accountNumber);
        
        // Simulate processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Reject a settlement.
     */
    @Transactional
    public SettlementDTO.SettlementResponse rejectSettlement(String settlementId, 
                                                              String reason, 
                                                              String rejectedBy) {
        SettlementEntity entity = settlementRepository.findByIdForUpdate(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if (entity.getStatus() != Settlement.SettlementStatus.PENDING &&
            entity.getStatus() != Settlement.SettlementStatus.PROCESSING) {
            throw new BusinessException("Can only reject pending or processing settlements");
        }

        entity.setStatus(Settlement.SettlementStatus.REJECTED);
        entity.setRejectionReason(reason);
        entity.setProcessedBy(rejectedBy);
        
        SettlementEntity savedEntity = settlementRepository.save(entity);

        eventPublisher.publishSettlementRejected(savedEntity.toDomain(), reason, rejectedBy);

        log.info("Rejected settlement {} reason: {}", settlementId, reason);
        return SettlementDTO.toResponse(savedEntity.toDomain());
    }

    /**
     * Apply an adjustment to a settlement.
     */
    @Transactional
    public SettlementDTO.SettlementResponse applyAdjustment(String settlementId, 
                                                             BigDecimal adjustmentAmount, 
                                                             String reason) {
        SettlementEntity entity = settlementRepository.findByIdForUpdate(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if (entity.getStatus() != Settlement.SettlementStatus.PENDING) {
            throw new BusinessException("Can only adjust pending settlements");
        }

        BigDecimal previousAmount = entity.getNetAmount();
        entity.setAdjustment(entity.getAdjustment().add(adjustmentAmount));
        entity.setNetAmount(entity.getNetAmount().add(adjustmentAmount));
        
        SettlementEntity savedEntity = settlementRepository.save(entity);

        eventPublisher.publishSettlementAdjusted(
                savedEntity.toDomain(), 
                previousAmount, 
                adjustmentAmount, 
                reason
        );

        log.info("Applied adjustment {} to settlement {}", adjustmentAmount, settlementId);
        return SettlementDTO.toResponse(savedEntity.toDomain());
    }

    /**
     * Get settlement by ID.
     */
    @Transactional(readOnly = true)
    public SettlementDTO.SettlementResponse getSettlement(String id) {
        SettlementEntity entity = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", id));
        return SettlementDTO.toResponse(entity.toDomain());
    }

    /**
     * List settlements by merchant.
     */
    @Transactional(readOnly = true)
    public List<SettlementDTO.SettlementResponse> getSettlementsByMerchant(String merchantId, 
                                                                            int page, 
                                                                            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SettlementEntity> entityPage = settlementRepository.findByMerchantId(merchantId, pageable);
        return entityPage.stream()
                .map(SettlementEntity::toDomain)
                .map(SettlementDTO::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get settlement summary for a merchant.
     */
    @Transactional(readOnly = true)
    public SettlementDTO.SettlementSummary getSettlementSummary(String merchantId) {
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Settlement.SettlementStatus status : Settlement.SettlementStatus.values()) {
            BigDecimal statusTotal = settlementRepository.getTotalAmountByMerchantAndStatus(merchantId, status);
            if (statusTotal != null) {
                totalNet = totalNet.add(statusTotal);
            }
        }

        return SettlementDTO.SettlementSummary.builder()
                .merchantId(merchantId)
                .totalGrossAmount(totalGross)
                .totalCommission(totalCommission)
                .totalTax(totalTax)
                .totalNetAmount(totalNet)
                .pendingCount((int) settlementRepository.countByMerchantIdAndStatus(
                        merchantId, Settlement.SettlementStatus.PENDING))
                .processingCount((int) settlementRepository.countByMerchantIdAndStatus(
                        merchantId, Settlement.SettlementStatus.PROCESSING))
                .completedCount((int) settlementRepository.countByMerchantIdAndStatus(
                        merchantId, Settlement.SettlementStatus.COMPLETED))
                .failedCount((int) settlementRepository.countByMerchantIdAndStatus(
                        merchantId, Settlement.SettlementStatus.FAILED))
                .build();
    }
}
