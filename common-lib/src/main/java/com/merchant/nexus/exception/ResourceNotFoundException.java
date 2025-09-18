package com.merchant.nexus.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends MerchantException {
    public ResourceNotFoundException(String resourceType, String id) {
        super("NOT_FOUND", 
              String.format("%s not found with id: %s", resourceType, id), 
              HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
