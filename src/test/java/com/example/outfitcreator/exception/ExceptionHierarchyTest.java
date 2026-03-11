package com.example.outfitcreator.exception;

import com.example.outfitcreator.photo.FileSizeExceededException;
import com.example.outfitcreator.photo.InvalidFileTypeException;
import com.example.outfitcreator.photo.StorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHierarchyTest {

    @Test
    void testOutfitCreatorExceptionBaseClass() {
        OutfitCreatorException exception = new OutfitCreatorException(
                "Test message",
                "TEST_ERROR",
                HttpStatus.BAD_REQUEST
        );

        assertEquals("Test message", exception.getMessage());
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Item not found");

        assertEquals("Item not found", exception.getMessage());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testValidationException() {
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("name", "Name is required");
        fieldErrors.put("email", "Invalid email format");

        ValidationException exception = new ValidationException("Validation failed", fieldErrors);

        assertEquals("Validation failed", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(2, exception.getFieldErrors().size());
        assertEquals("Name is required", exception.getFieldErrors().get("name"));
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        assertEquals("Invalid credentials", exception.getMessage());
        assertEquals("UNAUTHORIZED", exception.getErrorCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testForbiddenException() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        assertEquals("Access denied", exception.getMessage());
        assertEquals("FORBIDDEN", exception.getErrorCode());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testInvalidFileTypeException() {
        InvalidFileTypeException exception = new InvalidFileTypeException("Only JPEG allowed");

        assertEquals("Only JPEG allowed", exception.getMessage());
        assertEquals("INVALID_FILE_TYPE", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testFileSizeExceededException() {
        FileSizeExceededException exception = new FileSizeExceededException("File too large");

        assertEquals("File too large", exception.getMessage());
        assertEquals("FILE_SIZE_EXCEEDED", exception.getErrorCode());
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testStorageException() {
        StorageException exception = new StorageException("Storage failed");

        assertEquals("Storage failed", exception.getMessage());
        assertEquals("STORAGE_ERROR", exception.getErrorCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertTrue(exception instanceof OutfitCreatorException);
    }

    @Test
    void testStorageExceptionWithCause() {
        Throwable cause = new RuntimeException("Disk full");
        StorageException exception = new StorageException("Storage failed", cause);

        assertEquals("Storage failed", exception.getMessage());
        assertEquals("STORAGE_ERROR", exception.getErrorCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof OutfitCreatorException);
    }
}
