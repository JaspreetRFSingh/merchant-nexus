package com.merchant.nexus.repository;

import com.merchant.nexus.model.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

/**
 * JPA Entity for Merchant - demonstrates ORM mapping, auditing
 */
@Entity
@Table(name = "merchants")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class MerchantEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String businessName;

    @Column(name = "business_registration_number", nullable = false, length = 50, unique = true)
    private String businessRegistrationNumber;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Merchant.MerchantStatus status;

    @Embedded
    private AddressEntity address;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static MerchantEntity fromDomain(Merchant merchant) {
        MerchantEntity entity = new MerchantEntity();
        entity.setId(merchant.getId());
        entity.setBusinessName(merchant.getBusinessName());
        entity.setBusinessRegistrationNumber(merchant.getBusinessRegistrationNumber());
        entity.setOwnerName(merchant.getOwnerName());
        entity.setEmail(merchant.getEmail());
        entity.setPhone(merchant.getPhone());
        entity.setStatus(merchant.getStatus());
        if (merchant.getAddress() != null) {
            entity.setAddress(AddressEntity.fromDomain(merchant.getAddress()));
        }
        entity.setCreatedAt(merchant.getCreatedAt());
        entity.setUpdatedAt(merchant.getUpdatedAt());
        return entity;
    }

    public Merchant toDomain() {
        return Merchant.builder()
                .id(this.id)
                .businessName(this.businessName)
                .businessRegistrationNumber(this.businessRegistrationNumber)
                .ownerName(this.ownerName)
                .email(this.email)
                .phone(this.phone)
                .status(this.status)
                .address(this.address != null ? this.address.toDomain() : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
