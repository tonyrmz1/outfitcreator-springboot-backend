package com.example.outfitcreator.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Authenticated user is not allowed to perform the operation (HTTP 403).
 */
public class ForbiddenException extends OutfitCreatorException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
