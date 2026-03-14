package com.example.outfitcreator.outfit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating an outfit (name, notes, and optionally items).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an outfit's name, notes, and optionally items")
public class UpdateOutfitRequest {

    @Schema(description = "Outfit name", example = "Casual Friday")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Notes about the outfit", example = "Perfect for office casual days")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Schema(description = "List of clothing items with their positions (optional)")
    @Valid
    private List<CreateOutfitRequest.OutfitItemRequest> items;
}
