package com.example.outfitcreator.feature.auth.service;

import com.example.outfitcreator.feature.auth.dto.request.LoginRequest;
import com.example.outfitcreator.feature.auth.dto.request.RegisterRequest;
import com.example.outfitcreator.feature.auth.dto.request.UpdateProfileRequest;
import com.example.outfitcreator.feature.auth.dto.response.LoginResponse;
import com.example.outfitcreator.feature.auth.dto.response.UserDTO;

/**
 * Service interface for authentication and user profile management.
 */
public interface AuthService {

    /**
     * Register a new user.
     *
     * @param request the registration request containing user details
     * @return the created user DTO
     */
    UserDTO register(RegisterRequest request);

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param request the login request containing email and password
     * @return the login response with JWT token
     */
    LoginResponse login(LoginRequest request);

    /**
     * Get the current user's profile.
     *
     * @param userId the user ID
     * @return the user DTO
     */
    UserDTO getProfile(Long userId);

    /**
     * Update the current user's profile.
     *
     * @param userId the user ID
     * @param request the profile update request
     * @return the updated user DTO
     */
    UserDTO updateProfile(Long userId, UpdateProfileRequest request);
}