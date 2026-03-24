package com.example.outfitcreator.feature.outfit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API representation of an {@link com.example.outfitcreator.core.entity.Outfit} with nested items and compatibility metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Outfit details with clothing items and compatibility scores")
public class OutfitDTO {
    @Schema(description = "Outfit ID", example = "1")
    private Long id;
    
    @Schema(description = "Outfit name", example = "Casual Friday")
    private String name;
    
    @Schema(description = "Notes about the outfit", example = "Perfect for office casual days")
    private String notes;
    
    @Schema(description = "List of clothing items in the outfit")
    private List<OutfitItemDTO> items;
    
    @Schema(description = "Whether all items are still available", example = "true")
    private Boolean isComplete;
    
    @Schema(description = "Color compatibility score (0-100)", example = "85.0")
    private Double colorCompatibilityScore;
    
    @Schema(description = "Fit compatibility score (0-100)", example = "90.0")
    private Double fitCompatibilityScore;
    
    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;
}
