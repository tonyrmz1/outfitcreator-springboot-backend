package com.example.outfitcreator.feature.closet.dto.response;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Clothing item details")
public class ClothingItemDTO {

    @Schema(description = "Clothing item ID", example = "1")
    private Long id;

    @Schema(description = "Item name", example = "Blue Denim Jeans")
    private String name;

    @Schema(description = "Brand name", example = "Levi's")
    private String brand;

    @Schema(description = "Primary color", example = "blue")
    private String primaryColor;

    @Schema(description = "Secondary color (optional)", example = "white")
    private String secondaryColor;

    @Schema(description = "Clothing category", example = "BOTTOM")
    private ClothingCategory category;

    @Schema(description = "Size", example = "M")
    private String size;

    @Schema(description = "Season appropriateness", example = "ALL_SEASON")
    private Season season;

    @Schema(description = "Fit category", example = "REGULAR")
    private FitCategory fitCategory;

    @Schema(description = "Purchase date", example = "2023-01-15")
    private LocalDate purchaseDate;

    @Schema(description = "Photo URL", example = "http://localhost:8080/api/photos/item_1_1234567890.jpg")
    private String photoUrl;

    @Schema(description = "Number of times worn", example = "5")
    private Integer wearCount;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;
}
