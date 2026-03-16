package com.example.outfitcreator.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OutfitCreatorException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public OutfitCreatorException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public OutfitCreatorException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
