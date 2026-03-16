package com.example.outfitcreator.feature.auth.service;

import com.example.outfitcreator.feature.auth.dto.request.LoginRequest;
import com.example.outfitcreator.feature.auth.dto.request.RegisterRequest;
import com.example.outfitcreator.feature.auth.dto.request.UpdateProfileRequest;
import com.example.outfitcreator.feature.auth.dto.response.LoginResponse;
import com.example.outfitcreator.feature.auth.dto.response.UserDTO;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for authentication and user profile management.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user.
     *
     * @param request the registration request containing user details
     * @return the created user DTO
     */
    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        userRepository.save(user);

        return new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param request the login request containing email and password
     * @return the login response with JWT token
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new LoginResponse(token);
    }

    /**
     * Get the current user's profile.
     *
     * @param userId the user ID
     * @return the user DTO
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserDTO getProfile(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();

        return new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    /**
     * Update the current user's profile.
     *
     * @param userId the user ID
     * @param request the profile update request
     * @return the updated user DTO
     */
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        userRepository.save(user);

        return new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }
}