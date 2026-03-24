package com.example.outfitcreator.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base runtime exception for application-defined failures; carries HTTP status and a stable {@code errorCode}.
 */
@Getter
public class OutfitCreatorException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    /**
     * @param message    client-safe description
     * @param errorCode  machine-readable code (e.g. {@code RESOURCE_NOT_FOUND})
     * @param httpStatus HTTP status to return
     */
    public OutfitCreatorException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * @param cause chained root cause for logs
     */
    public OutfitCreatorException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
