package com.merchant.nexus.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Core Merchant domain model representing sellers on the Coupang Eats platform.
 * Demonstrates: Domain modeling, immutability patterns, audit trails
 */
@Data
@Builder
public class Merchant implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String businessName;
    private String businessRegistrationNumber;
    private String ownerName;
    private String email;
    private String phone;
    private MerchantStatus status;
    private Address address;
    private Instant createdAt;
    private Instant updatedAt;

    public enum MerchantStatus {
        PENDING_VERIFICATION,
        ACTIVE,
        SUSPENDED,
        DEACTIVATED
    }

    @Data
    @Builder
    public static class Address implements Serializable {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    public static Merchant createNew(String businessName, String businessRegistrationNumber, 
                                      String ownerName, String email, String phone) {
        return Merchant.builder()
                .id(UUID.randomUUID().toString())
                .businessName(businessName)
                .businessRegistrationNumber(businessRegistrationNumber)
                .ownerName(ownerName)
                .email(email)
                .phone(phone)
                .status(MerchantStatus.PENDING_VERIFICATION)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
