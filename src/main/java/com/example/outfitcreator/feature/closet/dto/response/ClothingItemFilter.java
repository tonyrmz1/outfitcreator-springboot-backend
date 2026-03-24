package com.example.outfitcreator.feature.closet.dto.response;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional filter criteria used when listing closet items (category, season, color, text search).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothingItemFilter {
    private ClothingCategory category;
    private Season season;
    private String color;
    private String searchQuery;
}
