package com.example.outfitcreator.recommendation;

import java.util.Map;

/**
 * ColorWheel utility for color theory calculations.
 * Maps color names to hue values on the color wheel (0-360 degrees).
 * Neutral colors are mapped to -1.
 */
public class ColorWheel {
    
    private static final Map<String, Integer> COLOR_HUES = Map.ofEntries(
        Map.entry("red", 0),
        Map.entry("orange", 30),
        Map.entry("yellow", 60),
        Map.entry("lime", 90),
        Map.entry("green", 120),
        Map.entry("cyan", 180),
        Map.entry("blue", 240),
        Map.entry("purple", 270),
        Map.entry("magenta", 300),
        Map.entry("pink", 330),
        Map.entry("brown", 25),
        Map.entry("white", -1),
        Map.entry("black", -1),
        Map.entry("gray", -1),
        Map.entry("beige", -1)
    );
    
    /**
     * Get the hue value for a color name.
     * 
     * @param color the color name (case-insensitive)
     * @return the hue value in degrees (0-360), or -1 for neutral colors
     */
    public static int getHue(String color) {
        if (color == null) {
            return -1;
        }
        return COLOR_HUES.getOrDefault(color.toLowerCase(), -1);
    }
    
    /**
     * Check if a color is neutral.
     * 
     * @param color the color name (case-insensitive)
     * @return true if the color is neutral (white, black, gray, beige), false otherwise
     */
    public static boolean isNeutral(String color) {
        return getHue(color) == -1;
    }
}
