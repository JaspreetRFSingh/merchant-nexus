package com.merchant.nexus.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessException extends MerchantException {
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}
