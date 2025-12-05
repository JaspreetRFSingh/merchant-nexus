package com.merchant.nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * JPA Repository for Settlement - demonstrates locking strategies for data consistency
 */
@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, String> {

    Page<SettlementEntity> findByMerchantId(String merchantId, Pageable pageable);

    Page<SettlementEntity> findByStatus(Settlement.SettlementStatus status, Pageable pageable);

    Page<SettlementEntity> findByMerchantIdAndStatus(String merchantId, 
                                                      Settlement.SettlementStatus status, 
                                                      Pageable pageable);

    /**
     * Pessimistic locking for preventing concurrent modifications during settlement processing.
     * Demonstrates: Data consistency, locking strategies
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SettlementEntity s WHERE s.id = :id")
    Optional<SettlementEntity> findByIdForUpdate(@Param("id") String id);

    /**
     * Calculate total settlement amount for a merchant.
     */
    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM SettlementEntity s WHERE s.merchantId = :merchantId AND s.status = :status")
    BigDecimal getTotalAmountByMerchantAndStatus(
        @Param("merchantId") String merchantId,
        @Param("status") Settlement.SettlementStatus status
    );

    /**
     * Count settlements by merchant and status.
     */
    long countByMerchantIdAndStatus(String merchantId, Settlement.SettlementStatus status);

    /**
     * Find oldest pending settlement for processing.
     */
    @Query("SELECT s FROM SettlementEntity s WHERE s.status = 'PENDING' ORDER BY s.requestedAt ASC")
    Page<SettlementEntity> findPendingSettlements(Pageable pageable);
}
