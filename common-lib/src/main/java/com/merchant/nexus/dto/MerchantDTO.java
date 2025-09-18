package com.merchant.nexus.dto;

import com.merchant.nexus.model.Merchant;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * DTOs for Merchant API - demonstrates request/response separation, validation
 */
public class MerchantDTO {

    @Data
    @Builder
    public static class CreateMerchantRequest {
        @NotBlank(message = "Business name is required")
        private String businessName;

        @NotBlank(message = "Business registration number is required")
        private String businessRegistrationNumber;

        @NotBlank(message = "Owner name is required")
        private String ownerName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        private AddressRequest address;
    }

    @Data
    @Builder
    public static class AddressRequest {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Data
    @Builder
    public static class MerchantResponse {
        private String id;
        private String businessName;
        private String businessRegistrationNumber;
        private String ownerName;
        private String email;
        private String phone;
        private String status;
        private AddressResponse address;
        @JsonProperty("createdAt")
        private Instant createdAt;
        @JsonProperty("updatedAt")
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class AddressResponse {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Data
    @Builder
    public static class MerchantListResponse {
        private List<MerchantResponse> merchants;
        private long total;
        private int page;
        private int size;
    }

    @Data
    @Builder
    public static class UpdateMerchantStatusRequest {
        @NotBlank(message = "Status is required")
        private String status;
    }

    public static MerchantResponse toResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .businessName(merchant.getBusinessName())
                .businessRegistrationNumber(merchant.getBusinessRegistrationNumber())
                .ownerName(merchant.getOwnerName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .status(merchant.getStatus().name())
                .address(merchant.getAddress() != null ? 
                    AddressResponse.builder()
                        .street(merchant.getAddress().getStreet())
                        .city(merchant.getAddress().getCity())
                        .state(merchant.getAddress().getState())
                        .postalCode(merchant.getAddress().getPostalCode())
                        .country(merchant.getAddress().getCountry())
                        .build() : null)
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }
}
