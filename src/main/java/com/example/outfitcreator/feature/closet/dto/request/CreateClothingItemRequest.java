package com.example.outfitcreator.feature.closet.dto.request;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new clothing item")
public class CreateClothingItemRequest {

    @Schema(description = "Item name", example = "Blue Denim Jeans", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Brand name", example = "Levi's")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Schema(description = "Primary color", example = "blue", required = true)
    @NotBlank(message = "Primary color is required")
    @Size(max = 50, message = "Primary color must not exceed 50 characters")
    private String primaryColor;

    @Schema(description = "Secondary color (optional)", example = "white")
    @Size(max = 50, message = "Secondary color must not exceed 50 characters")
    private String secondaryColor;

    @Schema(description = "Clothing category (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORIES)", example = "BOTTOM", required = true)
    @NotNull(message = "Category is required")
    private ClothingCategory category;

    @Schema(description = "Size", example = "M")
    @Size(max = 20, message = "Size must not exceed 20 characters")
    private String size;

    @Schema(description = "Season appropriateness (SPRING, SUMMER, AUTUMN, WINTER, ALL_SEASON)", example = "ALL_SEASON")
    private Season season;

    @Schema(description = "Fit category (TIGHT, REGULAR, LOOSE, OVERSIZED)", example = "REGULAR")
    private FitCategory fitCategory;

    @Schema(description = "Purchase date", example = "2023-01-15")
    @Past(message = "Purchase date must be in the past")
    private LocalDate purchaseDate;
}
