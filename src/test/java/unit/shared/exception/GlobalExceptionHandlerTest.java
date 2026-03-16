package unit.shared.exception;

import com.example.outfitcreator.shared.exception.ErrorResponse;
import com.example.outfitcreator.shared.exception.ForbiddenException;
import com.example.outfitcreator.shared.exception.GlobalExceptionHandler;
import com.example.outfitcreator.shared.exception.ResourceNotFoundException;
import com.example.outfitcreator.shared.exception.UnauthorizedException;
import com.example.outfitcreator.shared.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleOutfitCreatorException_shouldReturnErrorResponseWithPath() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Item not found");

        ResponseEntity<ErrorResponse> response = handler.handleOutfitCreatorException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Item not found");
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleOutfitCreatorException_withValidationException_shouldIncludeFieldErrors() {
        Map<String, String> fieldErrors = Map.of("email", "Email is required");
        ValidationException exception = new ValidationException("Validation failed", fieldErrors);

        ResponseEntity<ErrorResponse> response = handler.handleOutfitCreatorException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).isEqualTo(fieldErrors);
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void handleGenericException_shouldNotExposeInternalDetails() {
        Exception exception = new RuntimeException("Database connection failed: jdbc://localhost:5432/db");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).doesNotContain("Database").doesNotContain("jdbc");
    }

    @Test
    void handleOutfitCreatorException_withUnauthorizedException_shouldReturn401() {
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = handler.handleOutfitCreatorException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getErrorCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleOutfitCreatorException_withForbiddenException_shouldReturn403() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleOutfitCreatorException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FORBIDDEN");
    }
}
