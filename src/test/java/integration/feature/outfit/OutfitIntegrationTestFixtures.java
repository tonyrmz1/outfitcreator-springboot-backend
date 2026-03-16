package integration.feature.outfit;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.Outfit;
import com.example.outfitcreator.core.entity.OutfitItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.ItemPosition;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test fixtures for outfit integration tests.
 * Provides reusable test data creation methods.
 */
public final class OutfitIntegrationTestFixtures {

    private OutfitIntegrationTestFixtures() {
        // Utility class
    }

    // ===== User Fixtures =====

    public static User createTestUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setClothingItems(new ArrayList<>());
        user.setOutfits(new ArrayList<>());
        return user;
    }

    public static User createAnotherUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("password123");
        user.setFirstName("Another");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setClothingItems(new ArrayList<>());
        user.setOutfits(new ArrayList<>());
        return user;
    }

    // ===== ClothingItem Fixtures =====

    public static ClothingItem createTestClothingItem(Long id, User user, String name, ClothingCategory category, String color) {
        ClothingItem item = new ClothingItem();
        item.setId(id);
        item.setUser(user);
        item.setName(name);
        item.setPrimaryColor(color);
        item.setCategory(category);
        item.setSize("M");
        item.setSeason(Season.ALL_SEASON);
        item.setFitCategory(FitCategory.REGULAR);
        item.setWearCount(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    public static ClothingItem createTestTop(Long id, User user) {
        return createTestClothingItem(id, user, "Blue Shirt", ClothingCategory.TOP, "blue");
    }

    public static ClothingItem createTestBottom(Long id, User user) {
        return createTestClothingItem(id, user, "Black Pants", ClothingCategory.BOTTOM, "black");
    }

    public static ClothingItem createTestFootwear(Long id, User user) {
        return createTestClothingItem(id, user, "White Sneakers", ClothingCategory.FOOTWEAR, "white");
    }

    public static ClothingItem createTestOuterwear(Long id, User user) {
        return createTestClothingItem(id, user, "Black Jacket", ClothingCategory.OUTERWEAR, "black");
    }

    // ===== Outfit Fixtures =====

    public static Outfit createTestOutfit(Long id, User user) {
        Outfit outfit = new Outfit();
        outfit.setId(id);
        outfit.setUser(user);
        outfit.setName("Test Outfit");
        outfit.setNotes("Test notes");
        outfit.setIsComplete(true);
        outfit.setItems(new ArrayList<>());
        outfit.setCreatedAt(LocalDateTime.now());
        outfit.setUpdatedAt(LocalDateTime.now());
        return outfit;
    }

    public static Outfit createTestOutfitWithItems(Long id, User user, List<OutfitItem> items) {
        Outfit outfit = new Outfit();
        outfit.setId(id);
        outfit.setUser(user);
        outfit.setName("Test Outfit");
        outfit.setNotes("Test notes");
        outfit.setIsComplete(true);
        outfit.setItems(new ArrayList<>(items));
        outfit.setCreatedAt(LocalDateTime.now());
        outfit.setUpdatedAt(LocalDateTime.now());
        return outfit;
    }

    // ===== OutfitItem Fixtures =====

    public static OutfitItem createOutfitItem(Long id, Outfit outfit, ClothingItem clothingItem, ItemPosition position) {
        OutfitItem outfitItem = new OutfitItem();
        outfitItem.setId(id);
        outfitItem.setOutfit(outfit);
        outfitItem.setClothingItem(clothingItem);
        outfitItem.setPosition(position);
        return outfitItem;
    }

    // ===== Request DTO Builders =====

    public static CreateOutfitRequest createValidCreateRequest(Long topItemId, Long bottomItemId) {
        return CreateOutfitRequest.builder()
                .name("New Outfit")
                .notes("Outfit notes")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(topItemId)
                                .position(ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(bottomItemId)
                                .position(ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
    }

    public static CreateOutfitRequest createCreateRequestWithNameAndItems(String name, List<CreateOutfitRequest.OutfitItemRequest> items) {
        return CreateOutfitRequest.builder()
                .name(name)
                .items(items)
                .build();
    }

    public static UpdateOutfitRequest createValidUpdateRequest() {
        return UpdateOutfitRequest.builder()
                .name("Updated Outfit")
                .notes("Updated notes")
                .build();
    }

    public static UpdateOutfitRequest createUpdateRequestWithName(String name) {
        return UpdateOutfitRequest.builder()
                .name(name)
                .build();
    }

    public static UpdateOutfitRequest createUpdateRequestWithNotes(String notes) {
        return UpdateOutfitRequest.builder()
                .notes(notes)
                .build();
    }

    // ===== DTO Fixtures =====

    public static OutfitDTO createTestOutfitDTO(Long id, String name) {
        return OutfitDTO.builder()
                .id(id)
                .name(name)
                .notes("Test notes")
                .isComplete(true)
                .colorCompatibilityScore(85.0)
                .fitCompatibilityScore(90.0)
                .items(new ArrayList<>())
                .build();
    }
}