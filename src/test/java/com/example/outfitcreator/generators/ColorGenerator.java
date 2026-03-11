package com.example.outfitcreator.generators;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.util.List;

/**
 * jqwik generator for valid color names.
 * Generates colors that are recognized by the ColorWheel utility.
 */
public class ColorGenerator {
    
    private static final List<String> VALID_COLORS = List.of(
        "red", "orange", "yellow", "lime", "green", "cyan", 
        "blue", "purple", "magenta", "pink", "brown",
        "white", "black", "gray", "beige"
    );
    
    private static final List<String> NON_NEUTRAL_COLORS = List.of(
        "red", "orange", "yellow", "lime", "green", "cyan", 
        "blue", "purple", "magenta", "pink", "brown"
    );
    
    private static final List<String> NEUTRAL_COLORS = List.of(
        "white", "black", "gray", "beige"
    );
    
    /**
     * Generate any valid color name.
     */
    public static Arbitrary<String> validColors() {
        return Arbitraries.of(VALID_COLORS);
    }
    
    /**
     * Generate non-neutral color names (colors with hue values).
     */
    public static Arbitrary<String> nonNeutralColors() {
        return Arbitraries.of(NON_NEUTRAL_COLORS);
    }
    
    /**
     * Generate neutral color names (white, black, gray, beige).
     */
    public static Arbitrary<String> neutralColors() {
        return Arbitraries.of(NEUTRAL_COLORS);
    }
    
    /**
     * Generate optional secondary colors (can be null).
     */
    public static Arbitrary<String> optionalSecondaryColors() {
        return Arbitraries.of(VALID_COLORS).injectNull(0.3);
    }
}
