package com.example.outfitcreator.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends OutfitCreatorException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
