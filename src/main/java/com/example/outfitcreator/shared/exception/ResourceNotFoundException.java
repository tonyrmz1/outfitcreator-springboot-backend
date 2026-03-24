package com.example.outfitcreator.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested entity does not exist or is not visible to the caller (maps to 404).
 */
public class ResourceNotFoundException extends OutfitCreatorException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
