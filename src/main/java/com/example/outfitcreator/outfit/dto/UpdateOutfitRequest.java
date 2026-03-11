package com.example.outfitcreator.outfit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an outfit (name and notes only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an outfit's name and notes")
public class UpdateOutfitRequest {

    @Schema(description = "Outfit name", example = "Casual Friday")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Notes about the outfit", example = "Perfect for office casual days")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
