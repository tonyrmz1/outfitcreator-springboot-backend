package com.example.outfitcreator.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Authentication required or credentials invalid (HTTP 401).
 */
public class UnauthorizedException extends OutfitCreatorException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
