package com.example.outfitcreator.generators;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.FitCategory;
import com.example.outfitcreator.enums.Season;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * jqwik generator for ClothingItem entities with valid attribute combinations.
 * Ensures realistic test data with proper color, category, fit, and season combinations.
 */
public class ClothingItemGenerator {
    
    /**
     * Generate valid ClothingItem entities with all attributes.
     */
    public static Arbitrary<ClothingItem> clothingItems() {
        // Combine first 8 parameters
        Arbitrary<ClothingItem> baseItems = Combinators.combine(
            itemNames(),
            optionalBrands(),
            ColorGenerator.validColors(),
            ColorGenerator.optionalSecondaryColors(),
            CategoryGenerator.clothingCategories(),
            optionalSizes(),
            SeasonGenerator.optionalSeasons(),
            FitCategoryGenerator.optionalFitCategories()
        ).as((name, brand, primaryColor, secondaryColor, category, size, season, fitCategory) -> 
            ClothingItem.builder()
                .name(name)
                .brand(brand)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .category(category)
                .size(size)
                .season(season)
                .fitCategory(fitCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        // Add remaining attributes
        return Combinators.combine(
            baseItems,
            optionalPurchaseDates(),
            optionalPhotoPaths(),
            wearCounts()
        ).as((item, purchaseDate, photoPath, wearCount) -> {
            item.setPurchaseDate(purchaseDate);
            item.setPhotoPath(photoPath);
            item.setWearCount(wearCount);
            return item;
        });
    }
    
    /**
     * Generate ClothingItem entities with a specific user.
     */
    public static Arbitrary<ClothingItem> clothingItemsForUser(User user) {
        return clothingItems().map(item -> {
            item.setUser(user);
            return item;
        });
    }
    
    /**
     * Generate ClothingItem entities with a specific category.
     */
    public static Arbitrary<ClothingItem> clothingItemsWithCategory(ClothingCategory category) {
        // Combine first 8 parameters
        Arbitrary<ClothingItem> baseItems = Combinators.combine(
            itemNames(),
            optionalBrands(),
            ColorGenerator.validColors(),
            ColorGenerator.optionalSecondaryColors(),
            Arbitraries.just(category),
            optionalSizes(),
            SeasonGenerator.optionalSeasons(),
            FitCategoryGenerator.optionalFitCategories()
        ).as((name, brand, primaryColor, secondaryColor, cat, size, season, fitCategory) -> 
            ClothingItem.builder()
                .name(name)
                .brand(brand)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .category(cat)
                .size(size)
                .season(season)
                .fitCategory(fitCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        // Add remaining attributes
        return Combinators.combine(
            baseItems,
            optionalPurchaseDates(),
            optionalPhotoPaths(),
            wearCounts()
        ).as((item, purchaseDate, photoPath, wearCount) -> {
            item.setPurchaseDate(purchaseDate);
            item.setPhotoPath(photoPath);
            item.setWearCount(wearCount);
            return item;
        });
    }
    
    /**
     * Generate ClothingItem entities with a specific fit category.
     */
    public static Arbitrary<ClothingItem> clothingItemsWithFit(FitCategory fitCategory) {
        return clothingItems().map(item -> {
            item.setFitCategory(fitCategory);
            return item;
        });
    }
    
    /**
     * Generate ClothingItem entities with a specific season.
     */
    public static Arbitrary<ClothingItem> clothingItemsWithSeason(Season season) {
        return clothingItems().map(item -> {
            item.setSeason(season);
            return item;
        });
    }
    
    /**
     * Generate ClothingItem entities with a specific color.
     */
    public static Arbitrary<ClothingItem> clothingItemsWithColor(String color) {
        return clothingItems().map(item -> {
            item.setPrimaryColor(color);
            return item;
        });
    }
    
    /**
     * Generate realistic clothing item names based on category.
     */
    private static Arbitrary<String> itemNames() {
        return Arbitraries.of(
            // Tops
            "T-Shirt", "Polo Shirt", "Button-Up Shirt", "Blouse", "Tank Top", 
            "Sweater", "Hoodie", "Cardigan", "Dress Shirt",
            // Bottoms
            "Jeans", "Chinos", "Dress Pants", "Shorts", "Skirt", "Leggings",
            // Outerwear
            "Jacket", "Coat", "Blazer", "Windbreaker", "Parka", "Vest",
            // Footwear
            "Sneakers", "Boots", "Loafers", "Sandals", "Heels", "Flats",
            // Accessories
            "Belt", "Scarf", "Hat", "Watch", "Sunglasses", "Tie"
        );
    }
    
    /**
     * Generate optional brand names (can be null).
     */
    private static Arbitrary<String> optionalBrands() {
        return Arbitraries.of(
            "Nike", "Adidas", "Zara", "H&M", "Uniqlo", "Gap", 
            "Levi's", "Ralph Lauren", "Tommy Hilfiger", "Calvin Klein"
        ).injectNull(0.3);
    }
    
    /**
     * Generate optional sizes (can be null).
     */
    private static Arbitrary<String> optionalSizes() {
        return Arbitraries.of(
            "XS", "S", "M", "L", "XL", "XXL",
            "28", "30", "32", "34", "36", "38",
            "6", "7", "8", "9", "10", "11", "12"
        ).injectNull(0.2);
    }
    
    /**
     * Generate optional purchase dates (within the last 5 years, can be null).
     */
    private static Arbitrary<LocalDate> optionalPurchaseDates() {
        return Arbitraries.integers()
            .between(0, 1825) // 5 years in days
            .map(daysAgo -> LocalDate.now().minusDays(daysAgo))
            .injectNull(0.3);
    }
    
    /**
     * Generate optional photo paths (can be null).
     */
    private static Arbitrary<String> optionalPhotoPaths() {
        return Arbitraries.longs()
            .between(1L, 999999L)
            .map(id -> String.format("photos/item_%d_%d.jpg", id, System.currentTimeMillis()))
            .injectNull(0.4);
    }
    
    /**
     * Generate realistic wear counts (0-100).
     */
    private static Arbitrary<Integer> wearCounts() {
        return Arbitraries.integers().between(0, 100);
    }
    
    /**
     * Generate a list of ClothingItems representing a digital closet.
     */
    public static Arbitrary<java.util.List<ClothingItem>> digitalCloset() {
        return clothingItems().list().ofMinSize(5).ofMaxSize(50);
    }
    
    /**
     * Generate a balanced digital closet with items from all categories.
     */
    public static Arbitrary<java.util.List<ClothingItem>> balancedDigitalCloset() {
        return Combinators.combine(
            clothingItemsWithCategory(ClothingCategory.TOP).list().ofMinSize(2).ofMaxSize(10),
            clothingItemsWithCategory(ClothingCategory.BOTTOM).list().ofMinSize(2).ofMaxSize(10),
            clothingItemsWithCategory(ClothingCategory.FOOTWEAR).list().ofMinSize(1).ofMaxSize(5),
            clothingItemsWithCategory(ClothingCategory.OUTERWEAR).list().ofMinSize(0).ofMaxSize(5),
            clothingItemsWithCategory(ClothingCategory.ACCESSORIES).list().ofMinSize(0).ofMaxSize(5)
        ).as((tops, bottoms, footwear, outerwear, accessories) -> {
            java.util.List<ClothingItem> closet = new java.util.ArrayList<>();
            closet.addAll(tops);
            closet.addAll(bottoms);
            closet.addAll(footwear);
            closet.addAll(outerwear);
            closet.addAll(accessories);
            return closet;
        });
    }
}
