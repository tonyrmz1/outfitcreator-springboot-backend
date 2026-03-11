package com.example.outfitcreator.outfit.dto;

import com.example.outfitcreator.enums.ItemPosition;
import com.example.outfitcreator.item.dto.ClothingItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for outfit items.
 */
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
