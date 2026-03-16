package com.example.outfitcreator.feature.outfit.dto.response;

import com.example.outfitcreator.core.enums.ItemPosition;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Clothing item within an outfit with its position")
public class OutfitItemDTO {
    @Schema(description = "Outfit item ID", example = "1")
    private Long id;
    
    @Schema(description = "Clothing item details")
    private ClothingItemDTO clothingItem;
    
    @Schema(description = "Position in outfit (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY)", example = "TOP")
    private ItemPosition position;
}
