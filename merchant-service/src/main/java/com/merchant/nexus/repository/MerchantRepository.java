package com.merchant.nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Merchant - demonstrates repository pattern, custom queries
 */
@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, String> {

    Optional<MerchantEntity> findByEmail(String email);

    Optional<MerchantEntity> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    Page<MerchantEntity> findByStatus(Merchant.MerchantStatus status, Pageable pageable);

    @Query("SELECT m FROM MerchantEntity m WHERE " +
           "(:businessName IS NULL OR LOWER(m.businessName) LIKE LOWER(CONCAT('%', :businessName, '%'))) AND " +
           "(:status IS NULL OR m.status = :status)")
    Page<MerchantEntity> searchMerchants(
        @Param("businessName") String businessName,
        @Param("status") Merchant.MerchantStatus status,
        Pageable pageable
    );

    @Query("SELECT COUNT(m) FROM MerchantEntity m WHERE m.status = :status")
    long countByStatus(@Param("status") Merchant.MerchantStatus status);
}
