package com.example.outfitcreator.recommendation;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.FitCategory;
import com.example.outfitcreator.enums.Season;
import com.example.outfitcreator.recommendation.dto.OutfitRecommendation;
import com.example.outfitcreator.recommendation.dto.RecommendationRequest;
import com.example.outfitcreator.repository.ClothingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationGenerationTest {
    
    @Mock
    private ClothingItemRepository clothingItemRepository;
    
    private RecommendationEngine recommendationEngine;
    
    @BeforeEach
    void setUp() {
        recommendationEngine = new RecommendationEngine(clothingItemRepository);
    }
    
    @Test
    void generateRecommendations_shouldReturnEmptyListForEmptyCloset() {
        Long userId = 1L;
        when(clothingItemRepository.findByUserId(userId)).thenReturn(new ArrayList<>());
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).isEmpty();
    }
    
    @Test
    void generateRecommendations_shouldGenerateRecommendationsForValidCloset() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        items.add(createItem(1L, user, "Blue Shirt", "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(2L, user, "Beige Pants", "beige", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        items.add(createItem(3L, user, "Brown Shoes", "brown", ClothingCategory.FOOTWEAR, FitCategory.REGULAR, Season.ALL_SEASON, 0));
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations.get(0).getItems()).hasSize(3);
        assertThat(recommendations.get(0).getColorCompatibilityScore()).isGreaterThan(0);
        assertThat(recommendations.get(0).getFitCompatibilityScore()).isGreaterThan(0);
        assertThat(recommendations.get(0).getOverallScore()).isGreaterThan(0);
        assertThat(recommendations.get(0).getSeasonalAppropriateness()).isEqualTo("APPROPRIATE");
        assertThat(recommendations.get(0).getExplanation()).isNotEmpty();
    }
    
    @Test
    void generateRecommendations_shouldPrioritizeLessWornItems() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        items.add(createItem(1L, user, "Blue Shirt", "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 10));
        items.add(createItem(2L, user, "Red Shirt", "red", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(3L, user, "Beige Pants", "beige", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).isNotEmpty();
        // The first recommendation should include the red shirt (wear count 0) rather than blue shirt (wear count 10)
        boolean hasRedShirt = recommendations.get(0).getItems().stream()
            .anyMatch(item -> item.getName().equals("Red Shirt"));
        assertThat(hasRedShirt).isTrue();
    }
    
    @Test
    void generateRecommendations_shouldSkipLowScoringCombinations() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        // Tight-tight combination should be skipped (fit score 30.0 < 50.0)
        items.add(createItem(1L, user, "Tight Shirt", "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(2L, user, "Tight Pants", "red", ClothingCategory.BOTTOM, FitCategory.TIGHT, Season.SPRING, 0));
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        // Should be empty because the only combination has a low fit score
        assertThat(recommendations).isEmpty();
    }
    
    @Test
    void generateRecommendations_shouldRespectLimitParameter() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        // Create multiple tops and bottoms to generate many combinations
        for (int i = 0; i < 5; i++) {
            items.add(createItem((long) i, user, "Top " + i, "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
            items.add(createItem((long) (i + 10), user, "Bottom " + i, "beige", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        }
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(3)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).hasSizeLessThanOrEqualTo(3);
    }
    
    @Test
    void generateRecommendations_shouldApplySeasonFilter() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        items.add(createItem(1L, user, "Summer Shirt", "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SUMMER, 0));
        items.add(createItem(2L, user, "Winter Pants", "beige", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.WINTER, 0));
        items.add(createItem(3L, user, "Spring Shirt", "red", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(4L, user, "Spring Pants", "black", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).isNotEmpty();
        // All items should be spring or adjacent seasons
        recommendations.forEach(rec -> {
            assertThat(rec.getSeasonalAppropriateness()).isIn("APPROPRIATE", "WARNING");
        });
    }
    
    @Test
    void generateRecommendations_shouldApplyColorPreferenceFilter() {
        Long userId = 1L;
        User user = createUser(userId);
        
        List<ClothingItem> items = new ArrayList<>();
        items.add(createItem(1L, user, "Blue Shirt", "blue", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(2L, user, "Red Shirt", "red", ClothingCategory.TOP, FitCategory.TIGHT, Season.SPRING, 0));
        items.add(createItem(3L, user, "Blue Pants", "blue", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        items.add(createItem(4L, user, "Beige Pants", "beige", ClothingCategory.BOTTOM, FitCategory.LOOSE, Season.SPRING, 0));
        
        when(clothingItemRepository.findByUserId(userId)).thenReturn(items);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .season(Season.SPRING)
            .colorPreference("blue")
            .limit(10)
            .build();
        
        List<OutfitRecommendation> recommendations = 
            recommendationEngine.generateRecommendations(userId, request);
        
        assertThat(recommendations).isNotEmpty();
        // All recommendations should only include blue items
        recommendations.forEach(rec -> {
            rec.getItems().forEach(item -> {
                assertThat(item.getPrimaryColor()).isEqualTo("blue");
            });
        });
    }
    
    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        return user;
    }
    
    private ClothingItem createItem(Long id, User user, String name, String color, 
                                   ClothingCategory category, FitCategory fitCategory, 
                                   Season season, int wearCount) {
        ClothingItem item = new ClothingItem();
        item.setId(id);
        item.setUser(user);
        item.setName(name);
        item.setPrimaryColor(color);
        item.setCategory(category);
        item.setFitCategory(fitCategory);
        item.setSeason(season);
        item.setWearCount(wearCount);
        return item;
    }
}
