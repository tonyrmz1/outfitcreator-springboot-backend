package com.example.outfitcreator.recommendation;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.Season;
import com.example.outfitcreator.item.dto.ClothingItemDTO;
import com.example.outfitcreator.recommendation.dto.OutfitRecommendation;
import com.example.outfitcreator.recommendation.dto.RecommendationRequest;
import com.example.outfitcreator.repository.ClothingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RecommendationEngine service for generating outfit recommendations.
 * Implements color compatibility and fit compatibility algorithms based on color theory.
 */
@Service
@RequiredArgsConstructor
public class RecommendationEngine {
    
    private final ClothingItemRepository clothingItemRepository;
    
    /**
     * Calculate color compatibility score between two clothing items.
     * Uses color theory principles to determine harmony.
     * 
     * @param item1 the first clothing item
     * @param item2 the second clothing item
     * @return compatibility score (0-100), where higher is more compatible
     */
    public double calculateColorCompatibility(ClothingItem item1, ClothingItem item2) {
        String color1 = item1.getPrimaryColor();
        String color2 = item2.getPrimaryColor();
        
        // Neutrals always compatible
        if (ColorWheel.isNeutral(color1) || ColorWheel.isNeutral(color2)) {
            return 95.0;
        }
        
        int hue1 = ColorWheel.getHue(color1);
        int hue2 = ColorWheel.getHue(color2);
        
        // Calculate hue difference with wrap-around
        int hueDifference = Math.abs(hue1 - hue2);
        if (hueDifference > 180) {
            hueDifference = 360 - hueDifference;
        }
        
        // Determine harmony type and score
        if (hueDifference <= 30) {
            // Monochromatic or analogous (0-30 degrees)
            return 90.0;
        } else if (hueDifference >= 150 && hueDifference <= 210) {
            // Complementary (180 degrees ± 30)
            return 85.0;
        } else if (hueDifference >= 110 && hueDifference <= 130) {
            // Triadic (120 degrees ± 10)
            return 80.0;
        } else if (hueDifference >= 50 && hueDifference <= 70) {
            // Analogous extended (60 degrees ± 10)
            return 75.0;
        } else {
            // Less harmonious combinations
            return 50.0;
        }
    }


    /**
     * Calculate fit compatibility score between a top and bottom clothing item.
     * Ensures balanced outfit proportions by scoring different fit combinations.
     *
     * @param top the top clothing item
     * @param bottom the bottom clothing item
     * @return compatibility score (0-100), where higher is more compatible
     */
    public double calculateFitCompatibility(ClothingItem top, ClothingItem bottom) {
        var topFit = top.getFitCategory();
        var bottomFit = bottom.getFitCategory();

        // Avoid tight-tight combinations
        if (topFit == com.example.outfitcreator.enums.FitCategory.TIGHT &&
            bottomFit == com.example.outfitcreator.enums.FitCategory.TIGHT) {
            return 30.0;
        }

        // Avoid loose-loose combinations
        if (topFit == com.example.outfitcreator.enums.FitCategory.LOOSE &&
            bottomFit == com.example.outfitcreator.enums.FitCategory.LOOSE) {
            return 40.0;
        }

        // Avoid oversized-oversized combinations
        if (topFit == com.example.outfitcreator.enums.FitCategory.OVERSIZED &&
            bottomFit == com.example.outfitcreator.enums.FitCategory.OVERSIZED) {
            return 20.0;
        }

        // Excellent balance: tight-loose or loose-tight
        if ((topFit == com.example.outfitcreator.enums.FitCategory.TIGHT &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.LOOSE) ||
            (topFit == com.example.outfitcreator.enums.FitCategory.LOOSE &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.TIGHT)) {
            return 95.0;
        }

        // Good balance: tight-regular or regular-tight
        if ((topFit == com.example.outfitcreator.enums.FitCategory.TIGHT &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.REGULAR) ||
            (topFit == com.example.outfitcreator.enums.FitCategory.REGULAR &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.TIGHT)) {
            return 90.0;
        }

        // Good balance: loose-regular or regular-loose
        if ((topFit == com.example.outfitcreator.enums.FitCategory.LOOSE &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.REGULAR) ||
            (topFit == com.example.outfitcreator.enums.FitCategory.REGULAR &&
             bottomFit == com.example.outfitcreator.enums.FitCategory.LOOSE)) {
            return 85.0;
        }

        // Safe: regular-regular
        if (topFit == com.example.outfitcreator.enums.FitCategory.REGULAR &&
            bottomFit == com.example.outfitcreator.enums.FitCategory.REGULAR) {
            return 80.0;
        }

        // Other combinations
        return 70.0;
    }


    /**
     * Check if a clothing item is seasonally appropriate for the given season.
     * ALL_SEASON items are always appropriate.
     * Direct season matches are appropriate.
     * Adjacent seasons are also considered appropriate.
     *
     * Season temperature ranges:
     * - SPRING: 50-70°F (10-21°C)
     * - SUMMER: 71-90°F (22-32°C)
     * - AUTUMN: 40-69°F (4-21°C)
     * - WINTER: below 40°F (below 4°C)
     *
     * @param item the clothing item to check
     * @param currentSeason the current season
     * @return true if the item is seasonally appropriate, false otherwise
     */
    public boolean isSeasonallyAppropriate(ClothingItem item, com.example.outfitcreator.enums.Season currentSeason) {
        com.example.outfitcreator.enums.Season itemSeason = item.getSeason();

        // All-season items are always appropriate
        if (itemSeason == com.example.outfitcreator.enums.Season.ALL_SEASON) {
            return true;
        }

        // Direct match
        if (itemSeason == currentSeason) {
            return true;
        }

        // Adjacent seasons are acceptable
        return areAdjacentSeasons(itemSeason, currentSeason);
    }

    /**
     * Check if two seasons are adjacent to each other.
     * Adjacent seasons: spring-summer, summer-autumn, autumn-winter, winter-spring
     *
     * @param s1 the first season
     * @param s2 the second season
     * @return true if the seasons are adjacent, false otherwise
     */
    private boolean areAdjacentSeasons(com.example.outfitcreator.enums.Season s1, com.example.outfitcreator.enums.Season s2) {
        // Handle null or ALL_SEASON
        if (s1 == null || s2 == null ||
            s1 == com.example.outfitcreator.enums.Season.ALL_SEASON ||
            s2 == com.example.outfitcreator.enums.Season.ALL_SEASON) {
            return false;
        }

        // Define season order (circular)
        java.util.List<com.example.outfitcreator.enums.Season> seasonOrder = java.util.List.of(
            com.example.outfitcreator.enums.Season.WINTER,
            com.example.outfitcreator.enums.Season.SPRING,
            com.example.outfitcreator.enums.Season.SUMMER,
            com.example.outfitcreator.enums.Season.AUTUMN
        );

        int idx1 = seasonOrder.indexOf(s1);
        int idx2 = seasonOrder.indexOf(s2);

        // Calculate difference (considering wrap-around)
        int diff = Math.abs(idx1 - idx2);
        return diff == 1 || diff == 3; // Adjacent or wrap-around (winter-spring)
    }

    /**
     * Generate outfit recommendations for a user based on their digital closet.
     * Applies filters, calculates compatibility scores, and returns sorted recommendations.
     *
     * @param userId the user ID
     * @param request the recommendation request with filters and limit
     * @return list of outfit recommendations sorted by overall score
     */
    public List<OutfitRecommendation> generateRecommendations(Long userId, RecommendationRequest request) {
        // 1. Fetch user's clothing items
        List<ClothingItem> allItems = clothingItemRepository.findByUserId(userId);
        
        // 2. Apply filters (season, color preferences)
        List<ClothingItem> filteredItems = applyFilters(allItems, request);
        
        // 3. Group by category
        Map<ClothingCategory, List<ClothingItem>> itemsByCategory = 
            filteredItems.stream()
                .collect(Collectors.groupingBy(ClothingItem::getCategory));
        
        // 4. Get items by category and sort by wear count (prioritize less-worn items)
        List<ClothingItem> tops = sortByWearCount(
            itemsByCategory.getOrDefault(ClothingCategory.TOP, new ArrayList<>())
        );
        List<ClothingItem> bottoms = sortByWearCount(
            itemsByCategory.getOrDefault(ClothingCategory.BOTTOM, new ArrayList<>())
        );
        List<ClothingItem> footwear = sortByWearCount(
            itemsByCategory.getOrDefault(ClothingCategory.FOOTWEAR, new ArrayList<>())
        );
        List<ClothingItem> outerwear = sortByWearCount(
            itemsByCategory.getOrDefault(ClothingCategory.OUTERWEAR, new ArrayList<>())
        );
        
        // 5. Generate outfit combinations
        List<OutfitRecommendation> recommendations = new ArrayList<>();
        int limit = Math.min(request.getLimit() != null ? request.getLimit() : 10, 20);
        
        // Generate combinations with nested loops
        for (ClothingItem top : tops) {
            for (ClothingItem bottom : bottoms) {
                // Calculate compatibility scores
                double colorScore = calculateColorCompatibility(top, bottom);
                double fitScore = calculateFitCompatibility(top, bottom);
                
                // Skip low-scoring combinations (< 50.0)
                if (colorScore < 50.0 || fitScore < 50.0) {
                    continue;
                }
                
                // Find compatible footwear
                ClothingItem shoe = findBestFootwear(footwear, top, bottom);
                
                // Find compatible outerwear (optional)
                ClothingItem jacket = findBestOuterwear(outerwear, top, bottom);
                
                // Build recommendation
                OutfitRecommendation rec = buildRecommendation(
                    top, bottom, shoe, jacket, colorScore, fitScore, request.getSeason()
                );
                
                recommendations.add(rec);
                
                // Limit recommendations
                if (recommendations.size() >= limit) {
                    break;
                }
            }
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        // 6. Sort by overall score descending
        recommendations.sort(Comparator.comparingDouble(
            OutfitRecommendation::getOverallScore).reversed()
        );
        
        // 7. Return top N recommendations (max 20)
        return recommendations.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Apply filters to the list of clothing items based on the request.
     *
     * @param items the list of clothing items
     * @param request the recommendation request with filters
     * @return filtered list of clothing items
     */
    private List<ClothingItem> applyFilters(List<ClothingItem> items, RecommendationRequest request) {
        return items.stream()
            .filter(item -> {
                // Season filter
                if (request.getSeason() != null) {
                    if (!isSeasonallyAppropriate(item, request.getSeason())) {
                        return false;
                    }
                }
                
                // Color preference filter
                if (request.getColorPreference() != null && !request.getColorPreference().isEmpty()) {
                    String itemColor = item.getPrimaryColor();
                    if (itemColor == null || !itemColor.equalsIgnoreCase(request.getColorPreference())) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * Sort clothing items by wear count (ascending) to prioritize less-worn items.
     *
     * @param items the list of clothing items
     * @return sorted list with less-worn items first
     */
    private List<ClothingItem> sortByWearCount(List<ClothingItem> items) {
        return items.stream()
            .sorted(Comparator.comparingInt(item -> item.getWearCount() != null ? item.getWearCount() : 0))
            .collect(Collectors.toList());
    }

    /**
     * Find the best footwear that is compatible with the top and bottom.
     *
     * @param footwearList the list of available footwear
     * @param top the top clothing item
     * @param bottom the bottom clothing item
     * @return the best compatible footwear, or null if none found
     */
    private ClothingItem findBestFootwear(List<ClothingItem> footwearList, ClothingItem top, ClothingItem bottom) {
        if (footwearList.isEmpty()) {
            return null;
        }
        
        // Find footwear with best color compatibility
        return footwearList.stream()
            .max(Comparator.comparingDouble(shoe -> {
                double topScore = calculateColorCompatibility(shoe, top);
                double bottomScore = calculateColorCompatibility(shoe, bottom);
                return (topScore + bottomScore) / 2.0;
            }))
            .orElse(null);
    }

    /**
     * Find the best outerwear that is compatible with the top and bottom.
     *
     * @param outerwearList the list of available outerwear
     * @param top the top clothing item
     * @param bottom the bottom clothing item
     * @return the best compatible outerwear, or null if none found
     */
    private ClothingItem findBestOuterwear(List<ClothingItem> outerwearList, ClothingItem top, ClothingItem bottom) {
        if (outerwearList.isEmpty()) {
            return null;
        }
        
        // Find outerwear with best color compatibility
        return outerwearList.stream()
            .max(Comparator.comparingDouble(jacket -> {
                double topScore = calculateColorCompatibility(jacket, top);
                double bottomScore = calculateColorCompatibility(jacket, bottom);
                return (topScore + bottomScore) / 2.0;
            }))
            .orElse(null);
    }

    /**
     * Build an outfit recommendation from the selected items.
     *
     * @param top the top clothing item
     * @param bottom the bottom clothing item
     * @param shoe the footwear item (can be null)
     * @param jacket the outerwear item (can be null)
     * @param colorScore the color compatibility score
     * @param fitScore the fit compatibility score
     * @param season the requested season
     * @return the outfit recommendation
     */
    private OutfitRecommendation buildRecommendation(
            ClothingItem top, ClothingItem bottom, ClothingItem shoe,
            ClothingItem jacket, double colorScore, double fitScore, Season season) {
        
        // Build items list
        List<ClothingItemDTO> items = new ArrayList<>();
        items.add(toDTO(top));
        items.add(toDTO(bottom));
        if (shoe != null) {
            items.add(toDTO(shoe));
        }
        if (jacket != null) {
            items.add(toDTO(jacket));
        }
        
        // Build item positions map
        Map<String, String> itemPositions = new HashMap<>();
        itemPositions.put(top.getId().toString(), "TOP");
        itemPositions.put(bottom.getId().toString(), "BOTTOM");
        if (shoe != null) {
            itemPositions.put(shoe.getId().toString(), "FOOTWEAR");
        }
        if (jacket != null) {
            itemPositions.put(jacket.getId().toString(), "OUTERWEAR");
        }
        
        // Calculate overall score
        double overallScore = (colorScore + fitScore) / 2.0;
        
        // Check seasonal appropriateness
        boolean allAppropriate = season == null || items.stream()
            .allMatch(item -> {
                ClothingItem clothingItem = findItemById(top, bottom, shoe, jacket, item.getId());
                return clothingItem != null && isSeasonallyAppropriate(clothingItem, season);
            });
        
        String seasonalAppropriateness = allAppropriate ? "APPROPRIATE" : "WARNING";
        
        // Generate explanation
        String explanation = generateExplanation(colorScore, fitScore, allAppropriate);
        
        return OutfitRecommendation.builder()
            .items(items)
            .colorCompatibilityScore(colorScore)
            .fitCompatibilityScore(fitScore)
            .overallScore(overallScore)
            .seasonalAppropriateness(seasonalAppropriateness)
            .itemPositions(itemPositions)
            .explanation(explanation)
            .build();
    }

    /**
     * Find a clothing item by ID from the given items.
     *
     * @param top the top item
     * @param bottom the bottom item
     * @param shoe the shoe item
     * @param jacket the jacket item
     * @param id the ID to find
     * @return the clothing item, or null if not found
     */
    private ClothingItem findItemById(ClothingItem top, ClothingItem bottom, 
                                     ClothingItem shoe, ClothingItem jacket, Long id) {
        if (top.getId().equals(id)) return top;
        if (bottom.getId().equals(id)) return bottom;
        if (shoe != null && shoe.getId().equals(id)) return shoe;
        if (jacket != null && jacket.getId().equals(id)) return jacket;
        return null;
    }

    /**
     * Generate a human-readable explanation for the recommendation.
     *
     * @param colorScore the color compatibility score
     * @param fitScore the fit compatibility score
     * @param seasonallyAppropriate whether the outfit is seasonally appropriate
     * @return the explanation string
     */
    private String generateExplanation(double colorScore, double fitScore, boolean seasonallyAppropriate) {
        StringBuilder explanation = new StringBuilder();
        
        // Color explanation
        if (colorScore >= 90.0) {
            explanation.append("Excellent color harmony. ");
        } else if (colorScore >= 80.0) {
            explanation.append("Great color combination. ");
        } else if (colorScore >= 70.0) {
            explanation.append("Good color pairing. ");
        } else {
            explanation.append("Acceptable color match. ");
        }
        
        // Fit explanation
        if (fitScore >= 90.0) {
            explanation.append("Perfect fit balance. ");
        } else if (fitScore >= 80.0) {
            explanation.append("Well-balanced proportions. ");
        } else if (fitScore >= 70.0) {
            explanation.append("Good fit combination. ");
        } else {
            explanation.append("Acceptable fit pairing. ");
        }
        
        // Seasonal explanation
        if (!seasonallyAppropriate) {
            explanation.append("Note: Some items may not be ideal for the selected season.");
        }
        
        return explanation.toString().trim();
    }

    /**
     * Convert a ClothingItem entity to a ClothingItemDTO.
     *
     * @param item the clothing item entity
     * @return the clothing item DTO
     */
    private ClothingItemDTO toDTO(ClothingItem item) {
        return ClothingItemDTO.builder()
            .id(item.getId())
            .name(item.getName())
            .brand(item.getBrand())
            .primaryColor(item.getPrimaryColor())
            .secondaryColor(item.getSecondaryColor())
            .category(item.getCategory())
            .size(item.getSize())
            .season(item.getSeason())
            .fitCategory(item.getFitCategory())
            .purchaseDate(item.getPurchaseDate())
            .photoUrl(item.getPhotoPath())
            .wearCount(item.getWearCount())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }


}
