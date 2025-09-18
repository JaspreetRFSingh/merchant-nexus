package com.merchant.nexus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception hierarchy for the merchant system.
 * Demonstrates: Exception handling patterns, error categorization
 */
@Getter
public class MerchantException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public MerchantException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public MerchantException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
