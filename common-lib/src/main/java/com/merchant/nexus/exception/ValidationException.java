package com.merchant.nexus.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when data validation fails.
 */
public class ValidationException extends MerchantException {
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", 
              String.format("Validation failed for field '%s': %s", field, message), 
              HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }
}
