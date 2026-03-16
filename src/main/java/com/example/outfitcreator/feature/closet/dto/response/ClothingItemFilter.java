package com.example.outfitcreator.feature.closet.dto.response;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
