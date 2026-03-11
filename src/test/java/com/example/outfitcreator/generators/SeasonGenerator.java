package com.example.outfitcreator.generators;

import com.example.outfitcreator.enums.Season;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * jqwik generator for Season enum values.
 */
public class SeasonGenerator {
    
    /**
     * Generate any valid Season enum value.
     */
    public static Arbitrary<Season> seasons() {
        return Arbitraries.of(Season.class);
    }
    
    /**
     * Generate optional Season values (can be null).
     */
    public static Arbitrary<Season> optionalSeasons() {
        return Arbitraries.of(Season.class).injectNull(0.2);
    }
    
    /**
     * Generate specific seasons (excluding ALL_SEASON).
     */
    public static Arbitrary<Season> specificSeasons() {
        return Arbitraries.of(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER);
    }
}
