package com.example.outfitcreator.feature.auth.controller;

import com.example.outfitcreator.feature.auth.dto.request.LoginRequest;
import com.example.outfitcreator.feature.auth.dto.request.RegisterRequest;
import com.example.outfitcreator.feature.auth.dto.request.UpdateProfileRequest;
import com.example.outfitcreator.feature.auth.dto.response.LoginResponse;
import com.example.outfitcreator.feature.auth.dto.response.UserDTO;
import com.example.outfitcreator.shared.exception.ErrorResponse;
import com.example.outfitcreator.feature.auth.service.AuthService;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication and user profile management.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and profile management endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the created user DTO
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with email and password. The password is encrypted using BCrypt before storage."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = authService.register(request);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Authenticate a user and return a JWT token.
     *
     * @param request the login request
     * @return the login response with JWT token
     */
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user with email and password, returning a JWT token for subsequent API calls."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Get the current user's profile.
     *
     * @return the user DTO
     */
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the profile information for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        UserDTO userDTO = authService.getProfile(userId);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Update the current user's profile.
     *
     * @param request the profile update request
     * @return the updated user DTO
     */
    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information (first name, last name) for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        UserDTO userDTO = authService.updateProfile(userId, request);
        return ResponseEntity.ok(userDTO);
    }
}