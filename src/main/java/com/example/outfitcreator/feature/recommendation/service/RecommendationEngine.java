package com.example.outfitcreator.feature.recommendation.service;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.recommendation.dto.response.OutfitRecommendation;
import com.example.outfitcreator.feature.recommendation.dto.request.RecommendationRequest;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
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
        if (topFit == FitCategory.TIGHT &&
            bottomFit == FitCategory.TIGHT) {
            return 30.0;
        }

        // Avoid loose-loose combinations
        if (topFit == FitCategory.LOOSE &&
            bottomFit == FitCategory.LOOSE) {
            return 40.0;
        }

        // Avoid oversized-oversized combinations
        if (topFit == FitCategory.OVERSIZED &&
            bottomFit == FitCategory.OVERSIZED) {
            return 20.0;
        }

        // Excellent balance: tight-loose or loose-tight
        if ((topFit == FitCategory.TIGHT &&
             bottomFit == FitCategory.LOOSE) ||
            (topFit == FitCategory.LOOSE &&
             bottomFit == FitCategory.TIGHT)) {
            return 95.0;
        }

        // Good balance: tight-regular or regular-tight
        if ((topFit == FitCategory.TIGHT &&
             bottomFit == FitCategory.REGULAR) ||
            (topFit == FitCategory.REGULAR &&
             bottomFit == FitCategory.TIGHT)) {
            return 90.0;
        }

        // Good balance: loose-regular or regular-loose
        if ((topFit == FitCategory.LOOSE &&
             bottomFit == FitCategory.REGULAR) ||
            (topFit == FitCategory.REGULAR &&
             bottomFit == FitCategory.LOOSE)) {
            return 85.0;
        }

        // Safe: regular-regular
        if (topFit == FitCategory.REGULAR &&
            bottomFit == FitCategory.REGULAR) {
            return 80.0;
        }

        // Other combinations
        return 70.0;
    }

    /**
     * Check if a clothing item is seasonally appropriate for the given season.
     *
     * @param item the clothing item to check
     * @param currentSeason the current season
     * @return true if the item is seasonally appropriate, false otherwise
     */
    public boolean isSeasonallyAppropriate(ClothingItem item, Season currentSeason) {
        Season itemSeason = item.getSeason();

        // All-season items are always appropriate
        if (itemSeason == Season.ALL_SEASON) {
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
     *
     * @param s1 the first season
     * @param s2 the second season
     * @return true if the seasons are adjacent, false otherwise
     */
    private boolean areAdjacentSeasons(Season s1, Season s2) {
        if (s1 == null || s2 == null ||
            s1 == Season.ALL_SEASON ||
            s2 == Season.ALL_SEASON) {
            return false;
        }

        java.util.List<Season> seasonOrder = java.util.List.of(
            Season.WINTER,
            Season.SPRING,
            Season.SUMMER,
            Season.AUTUMN
        );

        int idx1 = seasonOrder.indexOf(s1);
        int idx2 = seasonOrder.indexOf(s2);

        int diff = Math.abs(idx1 - idx2);
        return diff == 1 || diff == 3;
    }

    /**
     * Generate outfit recommendations for a user based on their digital closet.
     *
     * @param userId the user ID
     * @param request the recommendation request with filters and limit
     * @return list of outfit recommendations sorted by overall score
     */
    public List<OutfitRecommendation> generateRecommendations(Long userId, RecommendationRequest request) {
        List<ClothingItem> allItems = clothingItemRepository.findByUserId(userId);
        List<ClothingItem> filteredItems = applyFilters(allItems, request);
        
        Map<ClothingCategory, List<ClothingItem>> itemsByCategory = 
            filteredItems.stream()
                .collect(Collectors.groupingBy(ClothingItem::getCategory));
        
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
        
        List<OutfitRecommendation> recommendations = new ArrayList<>();
        int limit = Math.min(request.getLimit() != null ? request.getLimit() : 10, 20);
        
        for (ClothingItem top : tops) {
            for (ClothingItem bottom : bottoms) {
                double colorScore = calculateColorCompatibility(top, bottom);
                double fitScore = calculateFitCompatibility(top, bottom);
                
                if (colorScore < 50.0 || fitScore < 50.0) {
                    continue;
                }
                
                ClothingItem shoe = findBestFootwear(footwear, top, bottom);
                ClothingItem jacket = findBestOuterwear(outerwear, top, bottom);
                
                OutfitRecommendation rec = buildRecommendation(
                    top, bottom, shoe, jacket, colorScore, fitScore, request.getSeason()
                );
                
                recommendations.add(rec);
                
                if (recommendations.size() >= limit) {
                    break;
                }
            }
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        recommendations.sort(Comparator.comparingDouble(
            OutfitRecommendation::getOverallScore).reversed()
        );
        
        return recommendations.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<ClothingItem> applyFilters(List<ClothingItem> items, RecommendationRequest request) {
        return items.stream()
            .filter(item -> {
                if (request.getSeason() != null) {
                    if (!isSeasonallyAppropriate(item, request.getSeason())) {
                        return false;
                    }
                }
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

    private List<ClothingItem> sortByWearCount(List<ClothingItem> items) {
        return items.stream()
            .sorted(Comparator.comparingInt(item -> item.getWearCount() != null ? item.getWearCount() : 0))
            .collect(Collectors.toList());
    }

    private ClothingItem findBestFootwear(List<ClothingItem> footwearList, ClothingItem top, ClothingItem bottom) {
        if (footwearList.isEmpty()) {
            return null;
        }
        return footwearList.stream()
            .max(Comparator.comparingDouble(shoe -> {
                double topScore = calculateColorCompatibility(shoe, top);
                double bottomScore = calculateColorCompatibility(shoe, bottom);
                return (topScore + bottomScore) / 2.0;
            }))
            .orElse(null);
    }

    private ClothingItem findBestOuterwear(List<ClothingItem> outerwearList, ClothingItem top, ClothingItem bottom) {
        if (outerwearList.isEmpty()) {
            return null;
        }
        return outerwearList.stream()
            .max(Comparator.comparingDouble(jacket -> {
                double topScore = calculateColorCompatibility(jacket, top);
                double bottomScore = calculateColorCompatibility(jacket, bottom);
                return (topScore + bottomScore) / 2.0;
            }))
            .orElse(null);
    }

    private OutfitRecommendation buildRecommendation(
            ClothingItem top, ClothingItem bottom, ClothingItem shoe,
            ClothingItem jacket, double colorScore, double fitScore, Season season) {
        
        List<ClothingItemDTO> items = new ArrayList<>();
        items.add(toDTO(top));
        items.add(toDTO(bottom));
        if (shoe != null) {
            items.add(toDTO(shoe));
        }
        if (jacket != null) {
            items.add(toDTO(jacket));
        }
        
        Map<String, String> itemPositions = new HashMap<>();
        itemPositions.put(top.getId().toString(), "TOP");
        itemPositions.put(bottom.getId().toString(), "BOTTOM");
        if (shoe != null) {
            itemPositions.put(shoe.getId().toString(), "FOOTWEAR");
        }
        if (jacket != null) {
            itemPositions.put(jacket.getId().toString(), "OUTERWEAR");
        }
        
        double overallScore = (colorScore + fitScore) / 2.0;
        
        boolean allAppropriate = season == null || items.stream()
            .allMatch(item -> {
                ClothingItem clothingItem = findItemById(top, bottom, shoe, jacket, item.getId());
                return clothingItem != null && isSeasonallyAppropriate(clothingItem, season);
            });
        
        String seasonalAppropriateness = allAppropriate ? "APPROPRIATE" : "WARNING";
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

    private ClothingItem findItemById(ClothingItem top, ClothingItem bottom, 
                                     ClothingItem shoe, ClothingItem jacket, Long id) {
        if (top.getId().equals(id)) return top;
        if (bottom.getId().equals(id)) return bottom;
        if (shoe != null && shoe.getId().equals(id)) return shoe;
        if (jacket != null && jacket.getId().equals(id)) return jacket;
        return null;
    }

    private String generateExplanation(double colorScore, double fitScore, boolean seasonallyAppropriate) {
        StringBuilder explanation = new StringBuilder();
        
        if (colorScore >= 90.0) {
            explanation.append("Excellent color harmony. ");
        } else if (colorScore >= 80.0) {
            explanation.append("Great color combination. ");
        } else if (colorScore >= 70.0) {
            explanation.append("Good color pairing. ");
        } else {
            explanation.append("Acceptable color match. ");
        }
        
        if (fitScore >= 90.0) {
            explanation.append("Perfect fit balance. ");
        } else if (fitScore >= 80.0) {
            explanation.append("Well-balanced proportions. ");
        } else if (fitScore >= 70.0) {
            explanation.append("Good fit combination. ");
        } else {
            explanation.append("Acceptable fit pairing. ");
        }
        
        if (!seasonallyAppropriate) {
            explanation.append("Note: Some items may not be ideal for the selected season.");
        }
        
        return explanation.toString().trim();
    }

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
            .photoUrl(generatePhotoUrl(item.getPhotoPath()))
            .wearCount(item.getWearCount())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }

    private String generatePhotoUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }
        String filename = photoPath.replace("\\", "/").substring(photoPath.replace("\\", "/").lastIndexOf('/') + 1);
        return String.format("%s/api/photos/%s", baseUrl, filename);
    }
}
