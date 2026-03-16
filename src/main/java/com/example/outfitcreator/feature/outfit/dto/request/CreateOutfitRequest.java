package com.example.outfitcreator.feature.outfit.dto.request;

import com.example.outfitcreator.core.enums.ItemPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new outfit")
public class CreateOutfitRequest {

    @Schema(description = "Outfit name", example = "Casual Friday", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Notes about the outfit", example = "Perfect for office casual days")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Schema(description = "List of clothing items with their positions", required = true)
    @NotEmpty(message = "At least one clothing item is required")
    @Valid
    private List<OutfitItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Clothing item with position in outfit")
    public static class OutfitItemRequest {
        @Schema(description = "Clothing item ID", example = "1", required = true)
        @NotNull(message = "Clothing item ID is required")
        private Long clothingItemId;

        @Schema(description = "Position in outfit (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY)", example = "TOP", required = true)
        @NotNull(message = "Position is required")
        private ItemPosition position;
    }
}
