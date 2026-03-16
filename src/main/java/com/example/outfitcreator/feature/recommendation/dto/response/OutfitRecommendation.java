package com.example.outfitcreator.feature.recommendation.dto.response;

import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO representing an outfit recommendation with compatibility scores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Outfit recommendation with compatibility scores and explanation")
public class OutfitRecommendation {
    @Schema(description = "List of clothing items in the recommended outfit")
    private List<ClothingItemDTO> items;
    
    @Schema(description = "Color compatibility score (0-100)", example = "85.0")
    private double colorCompatibilityScore;
    
    @Schema(description = "Fit compatibility score (0-100)", example = "90.0")
    private double fitCompatibilityScore;
    
    @Schema(description = "Overall recommendation score (0-100)", example = "87.5")
    private double overallScore;
    
    @Schema(description = "Seasonal appropriateness (APPROPRIATE, WARNING, NOT_APPROPRIATE)", example = "APPROPRIATE")
    private String seasonalAppropriateness;
    
    @Schema(description = "Map of item IDs to their positions in the outfit")
    private Map<String, String> itemPositions;
    
    @Schema(description = "Human-readable explanation of the recommendation", 
            example = "This outfit combines complementary colors with balanced fit proportions")
    private String explanation;
}
