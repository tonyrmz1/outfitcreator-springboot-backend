package com.example.outfitcreator.item.dto;

import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.Season;
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
}
