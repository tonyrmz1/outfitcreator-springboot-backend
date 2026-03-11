package com.example.outfitcreator.generators;

import com.example.outfitcreator.enums.FitCategory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * jqwik generator for FitCategory enum values.
 */
public class FitCategoryGenerator {
    
    /**
     * Generate any valid FitCategory enum value.
     */
    public static Arbitrary<FitCategory> fitCategories() {
        return Arbitraries.of(FitCategory.class);
    }
    
    /**
     * Generate optional FitCategory values (can be null).
     */
    public static Arbitrary<FitCategory> optionalFitCategories() {
        return Arbitraries.of(FitCategory.class).injectNull(0.2);
    }
    
    /**
     * Generate tight fit categories (TIGHT).
     */
    public static Arbitrary<FitCategory> tightFit() {
        return Arbitraries.just(FitCategory.TIGHT);
    }
    
    /**
     * Generate loose fit categories (LOOSE, OVERSIZED).
     */
    public static Arbitrary<FitCategory> looseFit() {
        return Arbitraries.of(FitCategory.LOOSE, FitCategory.OVERSIZED);
    }
    
    /**
     * Generate balanced fit categories (REGULAR).
     */
    public static Arbitrary<FitCategory> regularFit() {
        return Arbitraries.just(FitCategory.REGULAR);
    }
}
