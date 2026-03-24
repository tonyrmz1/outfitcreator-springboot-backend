package com.example.outfitcreator.feature.photo.exception;

import com.example.outfitcreator.shared.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

/**
 * Raised when upload MIME type is not among the allowed image types.
 */
public class InvalidFileTypeException extends OutfitCreatorException {
    public InvalidFileTypeException(String message) {
        super(message, "INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST);
    }
}
