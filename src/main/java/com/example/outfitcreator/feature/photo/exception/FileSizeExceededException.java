package com.example.outfitcreator.feature.photo.exception;

import com.example.outfitcreator.shared.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

/**
 * Raised when an uploaded file exceeds the configured maximum size.
 */
public class FileSizeExceededException extends OutfitCreatorException {
    public FileSizeExceededException(String message) {
        super(message, "FILE_SIZE_EXCEEDED", HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
