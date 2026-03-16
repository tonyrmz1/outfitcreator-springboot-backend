package unit.feature.outfit;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test fixtures and builders for outfit unit tests.
 * Provides reusable test data creation methods.
 */
public final class OutfitTestFixtures {

    private OutfitTestFixtures() {
        // Utility class
    }

    // ===== User Fixtures =====

    public static User.UserBuilder userBuilder() {
        return User.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static User createTestUser(Long id) {
        return userBuilder().id(id).build();
    }

    public static User createTestUser() {
        return userBuilder().build();
    }

    public static User createAnotherUser(Long id) {
        return User.builder()
                .email("another@example.com")
                .password("password123")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .id(id)
                .build();
    }

    // ===== ClothingItem Fixtures =====

    public static ClothingItem.ClothingItemBuilder clothingItemBuilder() {
        return ClothingItem.builder()
                .name("Test Item")
                .primaryColor("blue")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static ClothingItem createTestTop(Long id, User user) {
        return clothingItemBuilder()
                .id(id)
                .user(user)
                .name("Blue Shirt")
                .primaryColor("blue")
                .secondaryColor("white")
                .category(ClothingCategory.TOP)
                .size("M")
                .season(Season.ALL_SEASON)
                .fitCategory(FitCategory.REGULAR)
                .build();
    }

    public static ClothingItem createTestBottom(Long id, User user) {
        return clothingItemBuilder()
                .id(id)
                .user(user)
                .name("Black Pants")
                .primaryColor("black")
                .category(ClothingCategory.BOTTOM)
                .size("32")
                .season(Season.ALL_SEASON)
                .fitCategory(FitCategory.REGULAR)
                .build();
    }

    public static ClothingItem createTestFootwear(Long id, User user) {
        return clothingItemBuilder()
                .id(id)
                .user(user)
                .name("White Sneakers")
                .primaryColor("white")
                .category(ClothingCategory.FOOTWEAR)
                .size("10")
                .season(Season.ALL_SEASON)
                .fitCategory(FitCategory.REGULAR)
                .build();
    }

    public static ClothingItem createTestOuterwear(Long id, User user) {
        return clothingItemBuilder()
                .id(id)
                .user(user)
                .name("Black Jacket")
                .primaryColor("black")
                .category(ClothingCategory.OUTERWEAR)
                .size("L")
                .season(Season.ALL_SEASON)
                .fitCategory(FitCategory.REGULAR)
                .build();
    }

    public static ClothingItem createClothingItem(Long id, User user, ClothingCategory category, String color) {
        return clothingItemBuilder()
                .id(id)
                .user(user)
                .name(category.name() + " Item")
                .primaryColor(color)
                .category(category)
                .season(Season.ALL_SEASON)
                .fitCategory(FitCategory.REGULAR)
                .build();
    }

    // ===== Outfit Fixtures =====

    public static Outfit.OutfitBuilder outfitBuilder() {
        return Outfit.builder()
                .name("Test Outfit")
                .notes("Test notes")
                .isComplete(true)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static Outfit createTestOutfit(Long id, User user) {
        return outfitBuilder()
                .id(id)
                .user(user)
                .build();
    }

    public static Outfit createTestOutfitWithItems(Long id, User user, List<OutfitItem> items) {
        return outfitBuilder()
                .id(id)
                .user(user)
                .items(new ArrayList<>(items))
                .build();
    }

    // ===== OutfitItem Fixtures =====

    public static OutfitItem.OutfitItemBuilder outfitItemBuilder() {
        return OutfitItem.builder()
                .position(ItemPosition.TOP);
    }

    public static OutfitItem createOutfitItem(Long id, Outfit outfit, ClothingItem clothingItem, ItemPosition position) {
        return OutfitItem.builder()
                .id(id)
                .outfit(outfit)
                .clothingItem(clothingItem)
                .position(position)
                .build();
    }

    // ===== Request DTO Builders =====

    public static CreateOutfitRequest.CreateOutfitRequestBuilder createOutfitRequestBuilder() {
        return CreateOutfitRequest.builder()
                .name("New Outfit")
                .notes("Outfit notes");
    }

    public static CreateOutfitRequest createValidCreateRequest(Long topItemId, Long bottomItemId) {
        return createOutfitRequestBuilder()
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

    public static CreateOutfitRequest createCreateRequestWithItems(List<CreateOutfitRequest.OutfitItemRequest> items) {
        return createOutfitRequestBuilder()
                .items(items)
                .build();
    }

    public static UpdateOutfitRequest.UpdateOutfitRequestBuilder updateOutfitRequestBuilder() {
        return UpdateOutfitRequest.builder();
    }

    public static UpdateOutfitRequest createValidUpdateRequest() {
        return updateOutfitRequestBuilder()
                .name("Updated Outfit")
                .notes("Updated notes")
                .build();
    }

    public static UpdateOutfitRequest createUpdateRequestWithItems(List<CreateOutfitRequest.OutfitItemRequest> items) {
        return updateOutfitRequestBuilder()
                .items(items)
                .build();
    }

    // ===== DTO Fixtures =====

    public static OutfitDTO.OutfitDTOBuilder outfitDTOBuilder() {
        return OutfitDTO.builder()
                .name("Test Outfit")
                .notes("Test notes")
                .isComplete(true)
                .colorCompatibilityScore(85.0)
                .fitCompatibilityScore(90.0);
    }

    public static OutfitDTO createTestOutfitDTO(Long id) {
        return outfitDTOBuilder()
                .id(id)
                .items(new ArrayList<>())
                .build();
    }
}