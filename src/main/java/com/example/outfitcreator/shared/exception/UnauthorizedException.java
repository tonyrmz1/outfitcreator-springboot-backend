package com.example.outfitcreator.shared.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends OutfitCreatorException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
