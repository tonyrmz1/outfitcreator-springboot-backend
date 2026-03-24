package com.example.outfitcreator.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API error payload returned by {@link GlobalExceptionHandler}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response with details about the failure")
public class ErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    @Schema(description = "Error message", example = "Invalid request data")
    private String message;

    @Schema(description = "Application-specific error code", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "Request path that caused the error", example = "/api/clothing")
    private String path;
}
