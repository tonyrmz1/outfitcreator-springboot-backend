package com.example.outfitcreator.outfit;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.Outfit;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.ItemPosition;
import com.example.outfitcreator.exception.ForbiddenException;
import com.example.outfitcreator.exception.ResourceNotFoundException;
import com.example.outfitcreator.exception.ValidationException;
import com.example.outfitcreator.outfit.dto.CreateOutfitRequest;
import com.example.outfitcreator.outfit.dto.OutfitDTO;
import com.example.outfitcreator.outfit.dto.UpdateOutfitRequest;
import com.example.outfitcreator.repository.ClothingItemRepository;
import com.example.outfitcreator.repository.OutfitRepository;
import com.example.outfitcreator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for outfit update functionality.
 * Tests end-to-end flows, database persistence, and API contract compliance.
 * 
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 6.1, 6.2, 6.3, 6.4, 7.3, 7.4
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Outfit Update Integration Tests")
class OutfitUpdateIntegrationTest {

    @Autowired
    private OutfitService outfitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClothingItemRepository clothingItemRepository;

    @Autowired
    private OutfitRepository outfitRepository;

    private User testUser;
    private User anotherUser;
    private ClothingItem testTop;
    private ClothingItem testBottom;
    private ClothingItem testFootwear;
    private ClothingItem anotherUserItem;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Create another user
        anotherUser = User.builder()
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUser = userRepository.save(anotherUser);

        // Create test clothing items for testUser
        testTop = ClothingItem.builder()
                .user(testUser)
                .name("Blue Shirt")
                .primaryColor("blue")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTop = clothingItemRepository.save(testTop);

        testBottom = ClothingItem.builder()
                .user(testUser)
                .name("Black Pants")
                .primaryColor("black")
                .category(ClothingCategory.BOTTOM)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testBottom = clothingItemRepository.save(testBottom);

        testFootwear = ClothingItem.builder()
                .user(testUser)
                .name("White Sneakers")
                .primaryColor("white")
                .category(ClothingCategory.FOOTWEAR)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testFootwear = clothingItemRepository.save(testFootwear);

        // Create item for another user
        anotherUserItem = ClothingItem.builder()
                .user(anotherUser)
                .name("Red Jacket")
                .primaryColor("red")
                .category(ClothingCategory.OUTERWEAR)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUserItem = clothingItemRepository.save(anotherUserItem);
    }

    @Nested
    @DisplayName("7.1 End-to-End Update Flows")
    class EndToEndUpdateFlows {

        @Test
        @DisplayName("Create outfit → Update with new items → Verify response includes all items")
        void shouldUpdateOutfitWithNewItemsAndReturnComplete() {
            // Given - Create outfit with initial items
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Initial Outfit")
                    .notes("Initial notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Update with new items
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Outfit")
                    .notes("Updated notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testFootwear.getId())
                                    .position(ItemPosition.FOOTWEAR)
                                    .build()
                    ))
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), outfitId, updateRequest);

            // Then - Response includes all items with details
            assertThat(updated).isNotNull();
            assertThat(updated.getId()).isEqualTo(outfitId);
            assertThat(updated.getName()).isEqualTo("Updated Outfit");
            assertThat(updated.getNotes()).isEqualTo("Updated notes");
            assertThat(updated.getItems()).hasSize(3);
            assertThat(updated.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
            assertThat(updated.getItems().get(0).getClothingItem()).isNotNull();
            assertThat(updated.getItems().get(0).getClothingItem().getName()).isEqualTo("Blue Shirt");
            assertThat(updated.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
            assertThat(updated.getItems().get(1).getClothingItem().getName()).isEqualTo("Black Pants");
            assertThat(updated.getItems().get(2).getPosition()).isEqualTo(ItemPosition.FOOTWEAR);
            assertThat(updated.getItems().get(2).getClothingItem().getName()).isEqualTo("White Sneakers");
        }

        @Test
        @DisplayName("Create outfit → Update name only → Verify items unchanged")
        void shouldUpdateNameOnlyAndPreserveItems() {
            // Given - Create outfit with items
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .notes("Original Notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Update name only
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), outfitId, updateRequest);

            // Then - Name changes, items and notes unchanged
            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getNotes()).isEqualTo("Original Notes");
            assertThat(updated.getItems()).hasSize(2);
            assertThat(updated.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
            assertThat(updated.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
        }

        @Test
        @DisplayName("Create outfit → Update with invalid item → Verify error and no changes")
        void shouldFailUpdateWithInvalidItemAndRollback() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .notes("Original Notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Try to update with non-existent item
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(999999L)
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            // Then - Update fails
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                    .isInstanceOf(ValidationException.class);

            // And - Outfit remains unchanged
            OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
            assertThat(unchanged.getName()).isEqualTo("Original Name");
            assertThat(unchanged.getNotes()).isEqualTo("Original Notes");
            assertThat(unchanged.getItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("7.2 Database State Verification")
    class DatabaseStateVerification {

        @Test
        @DisplayName("Update → Query database → Verify changes persisted")
        void shouldPersistChangesToDatabase() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Update outfit
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();
            outfitService.update(testUser.getId(), outfitId, updateRequest);

            // Then - Query database directly and verify changes persisted
            Outfit outfit = outfitRepository.findById(outfitId).orElseThrow();
            assertThat(outfit.getName()).isEqualTo("Updated Name");
            assertThat(outfit.getItems()).hasSize(1);
            assertThat(outfit.getItems().get(0).getClothingItem().getId()).isEqualTo(testBottom.getId());
        }

        @Test
        @DisplayName("Update fails → Query database → Verify no changes")
        void shouldNotPersistChangesWhenUpdateFails() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Try to update with invalid item
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(999999L)
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();

            try {
                outfitService.update(testUser.getId(), outfitId, updateRequest);
            } catch (ValidationException e) {
                // Expected
            }

            // Then - Query database and verify no changes
            Outfit outfit = outfitRepository.findById(outfitId).orElseThrow();
            assertThat(outfit.getName()).isEqualTo("Original Name");
            assertThat(outfit.getItems()).hasSize(1);
            assertThat(outfit.getItems().get(0).getClothingItem().getId()).isEqualTo(testTop.getId());
        }
    }

    @Nested
    @DisplayName("7.3 API Contract and Response Formats")
    class ApiContractAndResponseFormats {

        @Test
        @DisplayName("Update request with items → Response includes items")
        void shouldIncludeItemsInResponseWhenUpdatedWithItems() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);

            // When - Update with items
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), created.getId(), updateRequest);

            // Then - Response includes all items with complete details
            assertThat(updated.getItems()).hasSize(2);
            assertThat(updated.getItems()).allMatch(item -> item.getClothingItem() != null);
            assertThat(updated.getItems()).allMatch(item -> item.getPosition() != null);
            assertThat(updated.getItems()).allMatch(item -> item.getClothingItem().getId() != null);
            assertThat(updated.getItems()).allMatch(item -> item.getClothingItem().getName() != null);
        }

        @Test
        @DisplayName("Update request without items → Response format unchanged")
        void shouldMaintainResponseFormatWhenUpdatingWithoutItems() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .notes("Test notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);

            // When - Update without items
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .notes("Updated notes")
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), created.getId(), updateRequest);

            // Then - Response has consistent format with all required fields
            assertThat(updated.getId()).isNotNull();
            assertThat(updated.getName()).isNotNull();
            assertThat(updated.getNotes()).isNotNull();
            assertThat(updated.getItems()).isNotNull();
            assertThat(updated.getIsComplete()).isNotNull();
            assertThat(updated.getColorCompatibilityScore()).isNotNull();
            assertThat(updated.getFitCompatibilityScore()).isNotNull();
            assertThat(updated.getCreatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Error responses → Correct status codes and formats")
        void shouldReturnCorrectErrorResponsesForDifferentScenarios() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When/Then - Non-existent item returns ValidationException
            UpdateOutfitRequest invalidItemRequest = UpdateOutfitRequest.builder()
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(999999L)
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, invalidItemRequest))
                    .isInstanceOf(ValidationException.class);

            // When/Then - Item from another user returns ForbiddenException
            UpdateOutfitRequest forbiddenItemRequest = UpdateOutfitRequest.builder()
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(anotherUserItem.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, forbiddenItemRequest))
                    .isInstanceOf(ForbiddenException.class);

            // When/Then - Non-existent outfit returns ResourceNotFoundException
            UpdateOutfitRequest validRequest = UpdateOutfitRequest.builder()
                    .name("Updated")
                    .build();
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), 999999L, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            // When/Then - Outfit from another user returns ForbiddenException
            assertThatThrownBy(() -> outfitService.update(anotherUser.getId(), outfitId, validRequest))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("Additional Validation Tests")
    class AdditionalValidationTests {

        @Test
        @DisplayName("Update with empty items list should preserve items (empty list treated as omitted)")
        void shouldPreserveItemsWhenUpdatingWithEmptyList() {
            // Given - Create outfit with items
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);

            // When - Update with empty items list (treated as omitted)
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated")
                    .items(List.of())
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), created.getId(), updateRequest);

            // Then - Items preserved (empty list is treated as omitted)
            assertThat(updated.getItems()).hasSize(2);
            assertThat(updated.getName()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("Update timestamp should reflect latest change")
        void shouldUpdateTimestampOnChange() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            LocalDateTime createdAt = created.getCreatedAt();
            LocalDateTime initialUpdatedAt = created.getUpdatedAt();

            // When - Update outfit
            try {
                Thread.sleep(100); // Ensure time difference
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .build();
            OutfitDTO updated = outfitService.update(testUser.getId(), created.getId(), updateRequest);

            // Then - updatedAt changed, createdAt unchanged
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
            assertThat(updated.getUpdatedAt()).isAfter(initialUpdatedAt);
        }

        @Test
        @DisplayName("Update with multiple items where one fails should rollback all changes")
        void shouldRollbackAllChangesWhenOneItemFails() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Try to update with multiple items, one invalid
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(999999L)
                                    .position(ItemPosition.FOOTWEAR)
                                    .build()
                    ))
                    .build();

            // Then - Update fails
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                    .isInstanceOf(ValidationException.class);

            // And - All changes rolled back (name and items)
            OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
            assertThat(unchanged.getName()).isEqualTo("Original Name");
            assertThat(unchanged.getItems()).hasSize(1);
            assertThat(unchanged.getItems().get(0).getClothingItem().getId()).isEqualTo(testTop.getId());
        }

        @Test
        @DisplayName("Update with item from another user should fail atomically")
        void shouldFailAtomicallyWhenItemBelongsToAnotherUser() {
            // Given - Create outfit
            CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                    .name("Original Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            OutfitDTO created = outfitService.create(testUser.getId(), createRequest);
            Long outfitId = created.getId();

            // When - Try to update with another user's item
            UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(anotherUserItem.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            // Then - Update fails
            assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                    .isInstanceOf(ForbiddenException.class);

            // And - No changes persisted
            OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
            assertThat(unchanged.getName()).isEqualTo("Original Name");
            assertThat(unchanged.getItems()).hasSize(1);
        }
    }
}
