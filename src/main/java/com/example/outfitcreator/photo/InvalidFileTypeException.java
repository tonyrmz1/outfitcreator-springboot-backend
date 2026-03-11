package com.example.outfitcreator.photo;

import com.example.outfitcreator.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends OutfitCreatorException {
    public InvalidFileTypeException(String message) {
        super(message, "INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST);
    }
}
