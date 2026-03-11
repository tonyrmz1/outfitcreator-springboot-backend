package com.example.outfitcreator.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user login response.
 */
@Schema(description = "User login response with JWT token")
public class LoginResponse {

    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;
    
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
