package com.merchant.nexus.repository;

import com.merchant.nexus.model.Merchant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Embeddable entity for Address - demonstrates value object persistence
 */
@Embeddable
@Getter
@Setter
public class AddressEntity {

    @Column(length = 255)
    private String street;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 50)
    private String country;

    public static AddressEntity fromDomain(Merchant.Address address) {
        AddressEntity entity = new AddressEntity();
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setPostalCode(address.getPostalCode());
        entity.setCountry(address.getCountry());
        return entity;
    }

    public Merchant.Address toDomain() {
        return Merchant.Address.builder()
                .street(this.street)
                .city(this.city)
                .state(this.state)
                .postalCode(this.postalCode)
                .country(this.country)
                .build();
    }
}
