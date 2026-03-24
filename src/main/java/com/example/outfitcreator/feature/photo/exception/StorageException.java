package com.example.outfitcreator.feature.photo.exception;

import com.example.outfitcreator.shared.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

/**
 * Raised for unexpected I/O failures while reading or writing image files.
 */
public class StorageException extends OutfitCreatorException {
    public StorageException(String message) {
        super(message, "STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
