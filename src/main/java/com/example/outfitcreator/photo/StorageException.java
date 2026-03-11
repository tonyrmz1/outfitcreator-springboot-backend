package com.example.outfitcreator.photo;

import com.example.outfitcreator.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

public class StorageException extends OutfitCreatorException {
    public StorageException(String message) {
        super(message, "STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
