package com.example.outfitcreator.photo;

import com.example.outfitcreator.exception.OutfitCreatorException;
import org.springframework.http.HttpStatus;

public class FileSizeExceededException extends OutfitCreatorException {
    public FileSizeExceededException(String message) {
        super(message, "FILE_SIZE_EXCEEDED", HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
