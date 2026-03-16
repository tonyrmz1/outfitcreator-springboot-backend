package com.example.outfitcreator.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user profile information.
 */
@Schema(description = "User profile information")
public class UserDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "User email address", example = "user@example.com")
    private String email;
    
    @Schema(description = "User first name", example = "John")
    private String firstName;
    
    @Schema(description = "User last name", example = "Doe")
    private String lastName;

    public UserDTO() {
    }

    public UserDTO(Long id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}