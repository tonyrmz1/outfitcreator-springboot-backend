package com.example.outfitcreator.outfit;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.FitCategory;
import com.example.outfitcreator.enums.Season;
import com.example.outfitcreator.item.ClothingItemService;
import com.example.outfitcreator.item.dto.CreateClothingItemRequest;
import com.example.outfitcreator.item.dto.UpdateClothingItemRequest;
import com.example.outfitcreator.outfit.dto.CreateOutfitRequest;
import com.example.outfitcreator.outfit.dto.OutfitDTO;
import com.example.outfitcreator.repository.ClothingItemRepository;
import com.example.outfitcreator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for outfit score recalculation functionality.
 * Tests Requirement 17.4: Outfit Score Recalculation
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OutfitScoreRecalculationTest {

    @Autowired
    private OutfitService outfitService;

    @Autowired
    private ClothingItemService clothingItemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClothingItemRepository clothingItemRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testScoreRecalculationWhenColorChanges() {
        // Create a red top and blue bottom
        var topRequest = CreateClothingItemRequest.builder()
                .name("Red Shirt")
                .primaryColor("red")
                .category(ClothingCategory.TOP)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var topDTO = clothingItemService.create(testUser.getId(), topRequest, null);

        var bottomRequest = CreateClothingItemRequest.builder()
                .name("Blue Jeans")
                .primaryColor("blue")
                .category(ClothingCategory.BOTTOM)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var bottomDTO = clothingItemService.create(testUser.getId(), bottomRequest, null);

        // Create outfit with these items
        var outfitRequest = CreateOutfitRequest.builder()
                .name("Test Outfit")
                .notes("Testing score recalculation")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(topDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(bottomDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
        var outfitDTO = outfitService.create(testUser.getId(), outfitRequest);

        // Get initial scores
        OutfitDTO retrievedOutfit = outfitService.getById(testUser.getId(), outfitDTO.getId());
        Double initialColorScore = retrievedOutfit.getColorCompatibilityScore();
        Double initialFitScore = retrievedOutfit.getFitCompatibilityScore();

        assertThat(initialColorScore).isNotNull();
        assertThat(initialFitScore).isNotNull();

        // Update the top's color to cyan (complementary to blue)
        var updateRequest = UpdateClothingItemRequest.builder()
                .name("Cyan Shirt")
                .primaryColor("cyan")
                .category(ClothingCategory.TOP)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        clothingItemService.update(testUser.getId(), topDTO.getId(), updateRequest);

        // Retrieve outfit again and check scores have been recalculated
        OutfitDTO updatedOutfit = outfitService.getById(testUser.getId(), outfitDTO.getId());
        Double newColorScore = updatedOutfit.getColorCompatibilityScore();
        Double newFitScore = updatedOutfit.getFitCompatibilityScore();

        assertThat(newColorScore).isNotNull();
        assertThat(newFitScore).isNotNull();
        
        // Color score should have changed (cyan and blue are complementary)
        assertThat(newColorScore).isNotEqualTo(initialColorScore);
        
        // Fit score should remain the same (fit didn't change)
        assertThat(newFitScore).isEqualTo(initialFitScore);
    }

    @Test
    void testScoreRecalculationWhenFitChanges() {
        // Create a tight top and regular bottom
        var topRequest = CreateClothingItemRequest.builder()
                .name("Tight Shirt")
                .primaryColor("black")
                .category(ClothingCategory.TOP)
                .fitCategory(FitCategory.TIGHT)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var topDTO = clothingItemService.create(testUser.getId(), topRequest, null);

        var bottomRequest = CreateClothingItemRequest.builder()
                .name("Regular Jeans")
                .primaryColor("blue")
                .category(ClothingCategory.BOTTOM)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var bottomDTO = clothingItemService.create(testUser.getId(), bottomRequest, null);

        // Create outfit
        var outfitRequest = CreateOutfitRequest.builder()
                .name("Fit Test Outfit")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(topDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(bottomDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
        var outfitDTO = outfitService.create(testUser.getId(), outfitRequest);

        // Get initial scores
        OutfitDTO retrievedOutfit = outfitService.getById(testUser.getId(), outfitDTO.getId());
        Double initialFitScore = retrievedOutfit.getFitCompatibilityScore();

        // Update the top's fit to loose (better balance with regular bottom)
        var updateRequest = UpdateClothingItemRequest.builder()
                .name("Loose Shirt")
                .primaryColor("black")
                .category(ClothingCategory.TOP)
                .fitCategory(FitCategory.LOOSE)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        clothingItemService.update(testUser.getId(), topDTO.getId(), updateRequest);

        // Retrieve outfit again and check fit score has changed
        OutfitDTO updatedOutfit = outfitService.getById(testUser.getId(), outfitDTO.getId());
        Double newFitScore = updatedOutfit.getFitCompatibilityScore();

        assertThat(newFitScore).isNotNull();
        assertThat(newFitScore).isNotEqualTo(initialFitScore);
    }

    @Test
    void testScoresCalculatedOnDemandForOldOutfits() {
        // Create clothing items
        var topRequest = CreateClothingItemRequest.builder()
                .name("White Shirt")
                .primaryColor("white")
                .category(ClothingCategory.TOP)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var topDTO = clothingItemService.create(testUser.getId(), topRequest, null);

        var bottomRequest = CreateClothingItemRequest.builder()
                .name("Black Pants")
                .primaryColor("black")
                .category(ClothingCategory.BOTTOM)
                .fitCategory(FitCategory.REGULAR)
                .season(Season.ALL_SEASON)
                .purchaseDate(LocalDate.now().minusDays(10))
                .build();
        var bottomDTO = clothingItemService.create(testUser.getId(), bottomRequest, null);

        // Create outfit
        var outfitRequest = CreateOutfitRequest.builder()
                .name("Classic Outfit")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(topDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(bottomDTO.getId())
                                .position(com.example.outfitcreator.enums.ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
        var outfitDTO = outfitService.create(testUser.getId(), outfitRequest);

        // Retrieve outfit - scores should be calculated on-demand
        OutfitDTO retrievedOutfit = outfitService.getById(testUser.getId(), outfitDTO.getId());

        assertThat(retrievedOutfit.getColorCompatibilityScore()).isNotNull();
        assertThat(retrievedOutfit.getFitCompatibilityScore()).isNotNull();
        
        // Neutrals (white and black) should have high color compatibility
        assertThat(retrievedOutfit.getColorCompatibilityScore()).isGreaterThan(90.0);
    }
}
