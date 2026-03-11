package com.example.outfitcreator.recommendation.dto;

import com.example.outfitcreator.enums.Season;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for outfit recommendations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request parameters for generating outfit recommendations")
public class RecommendationRequest {
    @Schema(description = "Filter by season (SPRING, SUMMER, AUTUMN, WINTER, ALL_SEASON)", example = "SPRING")
    private Season season;
    
    @Schema(description = "Filter by occasion", example = "casual")
    @Size(max = 100, message = "Occasion must not exceed 100 characters")
    private String occasion;
    
    @Schema(description = "Preferred color palette", example = "blue")
    @Size(max = 50, message = "Color preference must not exceed 50 characters")
    private String colorPreference;
    
    @Schema(description = "Number of recommendations to return (1-20)", example = "10")
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 20, message = "Limit must not exceed 20")
    @Builder.Default
    private Integer limit = 10;
}
