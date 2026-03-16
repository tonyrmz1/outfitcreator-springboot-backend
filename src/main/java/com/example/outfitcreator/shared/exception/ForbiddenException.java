package com.example.outfitcreator.shared.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends OutfitCreatorException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
