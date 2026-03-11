package com.example.outfitcreator.generators;

import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.ItemPosition;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * jqwik generator for ClothingCategory and ItemPosition enum values.
 */
public class CategoryGenerator {
    
    /**
     * Generate any valid ClothingCategory enum value.
     */
    public static Arbitrary<ClothingCategory> clothingCategories() {
        return Arbitraries.of(ClothingCategory.class);
    }
    
    /**
     * Generate any valid ItemPosition enum value.
     */
    public static Arbitrary<ItemPosition> itemPositions() {
        return Arbitraries.of(ItemPosition.class);
    }
    
    /**
     * Generate top categories (TOP, OUTERWEAR).
     */
    public static Arbitrary<ClothingCategory> topCategories() {
        return Arbitraries.of(ClothingCategory.TOP, ClothingCategory.OUTERWEAR);
    }
    
    /**
     * Generate bottom categories (BOTTOM).
     */
    public static Arbitrary<ClothingCategory> bottomCategories() {
        return Arbitraries.just(ClothingCategory.BOTTOM);
    }
    
    /**
     * Generate footwear categories (FOOTWEAR).
     */
    public static Arbitrary<ClothingCategory> footwearCategories() {
        return Arbitraries.just(ClothingCategory.FOOTWEAR);
    }
    
    /**
     * Generate accessory categories (ACCESSORIES).
     */
    public static Arbitrary<ClothingCategory> accessoryCategories() {
        return Arbitraries.just(ClothingCategory.ACCESSORIES);
    }
}
