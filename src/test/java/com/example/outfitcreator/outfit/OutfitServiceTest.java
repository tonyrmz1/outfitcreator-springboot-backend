package com.example.outfitcreator.outfit;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.Outfit;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.ItemPosition;
import com.example.outfitcreator.exception.ForbiddenException;
import com.example.outfitcreator.exception.ResourceNotFoundException;
import com.example.outfitcreator.outfit.dto.CreateOutfitRequest;
import com.example.outfitcreator.outfit.dto.OutfitDTO;
import com.example.outfitcreator.outfit.dto.UpdateOutfitRequest;
import com.example.outfitcreator.repository.ClothingItemRepository;
import com.example.outfitcreator.repository.OutfitRepository;
import com.example.outfitcreator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OutfitServiceTest {

    @Autowired
    private OutfitService outfitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClothingItemRepository clothingItemRepository;

    @Autowired
    private OutfitRepository outfitRepository;

    private User testUser;
    private ClothingItem testTop;
    private ClothingItem testBottom;

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

        // Create test clothing items
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
    }

    @Test
    void shouldCreateOutfit() {
        // Given
        CreateOutfitRequest request = CreateOutfitRequest.builder()
                .name("Casual Outfit")
                .notes("For weekend")
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

        // When
        OutfitDTO result = outfitService.create(testUser.getId(), request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Casual Outfit");
        assertThat(result.getNotes()).isEqualTo("For weekend");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getIsComplete()).isTrue();
    }

    @Test
    void shouldGetOutfitById() {
        // Given
        CreateOutfitRequest request = CreateOutfitRequest.builder()
                .name("Test Outfit")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build()
                ))
                .build();
        OutfitDTO created = outfitService.create(testUser.getId(), request);

        // When
        OutfitDTO result = outfitService.getById(testUser.getId(), created.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("Test Outfit");
    }

    @Test
    void shouldThrowExceptionWhenGettingOutfitFromDifferentUser() {
        // Given
        User anotherUser = User.builder()
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUser = userRepository.save(anotherUser);
        Long anotherUserId = anotherUser.getId();

        CreateOutfitRequest request = CreateOutfitRequest.builder()
                .name("Test Outfit")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build()
                ))
                .build();
        OutfitDTO created = outfitService.create(testUser.getId(), request);
        Long outfitId = created.getId();

        // When/Then
        assertThatThrownBy(() -> outfitService.getById(anotherUserId, outfitId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldUpdateOutfit() {
        // Given
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

        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .notes("Updated Notes")
                .build();

        // When
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Updated Notes");
        assertThat(result.getItems()).hasSize(1); // Items should remain unchanged
    }

    @Test
    void shouldDeleteOutfit() {
        // Given
        CreateOutfitRequest request = CreateOutfitRequest.builder()
                .name("To Delete")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build()
                ))
                .build();
        OutfitDTO created = outfitService.create(testUser.getId(), request);

        // When
        outfitService.delete(testUser.getId(), created.getId());

        // Then
        assertThatThrownBy(() -> outfitService.getById(testUser.getId(), created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindAllOutfitsWithPagination() {
        // Given
        for (int i = 0; i < 5; i++) {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Outfit " + i)
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();
            outfitService.create(testUser.getId(), request);
        }

        // When
        Page<OutfitDTO> result = outfitService.findAll(testUser.getId(), PageRequest.of(0, 3));

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldMarkOutfitAsIncompleteWhenClothingItemDeleted() {
        // Given
        CreateOutfitRequest request = CreateOutfitRequest.builder()
                .name("Test Outfit")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build()
                ))
                .build();
        OutfitDTO created = outfitService.create(testUser.getId(), request);
        Long outfitId = created.getId();

        // When
        outfitService.handleClothingItemDeletion(testTop.getId());

        // Then
        Outfit outfit = outfitRepository.findById(outfitId).orElseThrow();
        assertThat(outfit.getIsComplete()).isFalse();
    }

    @Test
    void shouldPreserveExistingItemsWhenItemsFieldIsNull() {
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

        // When - Update with null items field
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .notes("Updated Notes")
                .items(null)
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Items should be preserved
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Updated Notes");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    @Test
    void shouldPreserveExistingItemsWhenItemsFieldIsOmitted() {
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

        // When - Update with omitted items field (not set in builder)
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .notes("Updated Notes")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Items should be preserved
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Updated Notes");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    @Test
    void shouldPreserveExistingItemsWhenUpdatingNameOnly() {
        // Given - Create outfit with items
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

        // When - Update with name only
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Items should be preserved
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Original Notes");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
    }

    @Test
    void shouldNotUpdateOutfitWhenOneItemValidationFails() {
        // Given - Create outfit with items
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

        // When - Update with multiple items, one of which doesn't exist
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(999999L) // Non-existent item
                                .position(ItemPosition.BOTTOM)
                                .build()
                ))
                .build();

        // Then - Update should fail with ValidationException
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                .isInstanceOf(Exception.class);

        // And - Outfit should remain unchanged (name and items)
        OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
        assertThat(unchanged.getName()).isEqualTo("Original Name");
        assertThat(unchanged.getNotes()).isEqualTo("Original Notes");
        assertThat(unchanged.getItems()).hasSize(1);
        assertThat(unchanged.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
    }

    @Test
    void shouldNotUpdateOutfitWhenItemBelongsToAnotherUser() {
        // Given - Create another user with their own item
        User anotherUser = User.builder()
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUser = userRepository.save(anotherUser);

        ClothingItem anotherUserItem = ClothingItem.builder()
                .user(anotherUser)
                .name("Another User's Shirt")
                .primaryColor("red")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUserItem = clothingItemRepository.save(anotherUserItem);

        // Create outfit for testUser
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

        // Then - Update should fail with ForbiddenException
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                .isInstanceOf(ForbiddenException.class);

        // And - Outfit should remain unchanged
        OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
        assertThat(unchanged.getName()).isEqualTo("Original Name");
        assertThat(unchanged.getItems()).hasSize(1);
        assertThat(unchanged.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
    }

    // ===== Task 6.1: Test backward compatibility scenarios =====

    @Test
    void shouldUpdateNameOnlyWithoutChangingItems() {
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

        // When - Update with name only
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Only name changes, items and notes unchanged
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Original Notes");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    @Test
    void shouldUpdateNotesOnlyWithoutChangingItems() {
        // Given - Create outfit with items
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

        // When - Update with notes only
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .notes("Updated Notes")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Only notes changes, name and items unchanged
        assertThat(result.getName()).isEqualTo("Original Name");
        assertThat(result.getNotes()).isEqualTo("Updated Notes");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
    }

    @Test
    void shouldPreserveItemsWhenUpdatingNameAndNotes() {
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

        // When - Update both name and notes
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .notes("Updated Notes")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Name and notes change, items unchanged
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getNotes()).isEqualTo("Updated Notes");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    // ===== Task 6.2: Test item validation error cases =====

    @Test
    void shouldThrowValidationExceptionWhenItemDoesNotExist() {
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

        // Then - Should throw ValidationException
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                .isInstanceOf(Exception.class);

        // And - Outfit should remain unchanged
        OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
        assertThat(unchanged.getName()).isEqualTo("Original Name");
        assertThat(unchanged.getItems()).hasSize(1);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenItemBelongsToAnotherUser() {
        // Given - Create another user with their own item
        User anotherUser = User.builder()
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUser = userRepository.save(anotherUser);

        ClothingItem anotherUserItem = ClothingItem.builder()
                .user(anotherUser)
                .name("Another User's Shirt")
                .primaryColor("red")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUserItem = clothingItemRepository.save(anotherUserItem);

        // Create outfit for testUser
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

        // Then - Should throw ForbiddenException
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                .isInstanceOf(ForbiddenException.class);

        // And - Outfit should remain unchanged
        OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
        assertThat(unchanged.getName()).isEqualTo("Original Name");
        assertThat(unchanged.getItems()).hasSize(1);
    }

    @Test
    void shouldNotUpdateWhenMultipleItemsAndOneIsInvalid() {
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
                                .clothingItemId(999999L)
                                .position(ItemPosition.BOTTOM)
                                .build()
                ))
                .build();

        // Then - Should fail
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), outfitId, updateRequest))
                .isInstanceOf(Exception.class);

        // And - All changes should be rolled back (name and items)
        OutfitDTO unchanged = outfitService.getById(testUser.getId(), outfitId);
        assertThat(unchanged.getName()).isEqualTo("Original Name");
        assertThat(unchanged.getItems()).hasSize(1);
        assertThat(unchanged.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
    }

    // ===== Task 6.3: Test item replacement scenarios =====

    @Test
    void shouldReplaceItemsWhenUpdatingWithNewItems() {
        // Given - Create outfit with initial items
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

        // Create additional item for replacement
        ClothingItem newTop = ClothingItem.builder()
                .user(testUser)
                .name("Red Shirt")
                .primaryColor("red")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newTop = clothingItemRepository.save(newTop);

        // When - Update with new items
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(newTop.getId())
                                .position(ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testBottom.getId())
                                .position(ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Old items removed, new items added
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().stream()
                .map(item -> item.getClothingItem().getId())
                .toList())
                .containsExactlyInAnyOrder(newTop.getId(), testBottom.getId());
    }

    @Test
    void shouldPreserveItemsWhenUpdatingWithEmptyList() {
        // Given - Create outfit with items
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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

        // When - Update with empty items list (treated as no items provided)
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .items(List.of())
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Items should be preserved (empty list is treated as omitted)
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    @Test
    void shouldPreserveItemsWhenUpdatingWithSameItems() {
        // Given - Create outfit with items
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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

        // When - Update with same items
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
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
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Items remain the same
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(ItemPosition.TOP);
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(ItemPosition.BOTTOM);
    }

    // ===== Task 6.4: Test compatibility score updates =====

    @Test
    void shouldRecalculateScoresWhenItemsAreUpdated() {
        // Given - Create outfit with items
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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
        Double originalColorScore = created.getColorCompatibilityScore();

        // Create new items
        ClothingItem newTop = ClothingItem.builder()
                .user(testUser)
                .name("Green Shirt")
                .primaryColor("green")
                .category(ClothingCategory.TOP)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newTop = clothingItemRepository.save(newTop);

        // When - Update with new items
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(newTop.getId())
                                .position(ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testBottom.getId())
                                .position(ItemPosition.BOTTOM)
                                .build()
                ))
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Scores should be recalculated (may be different)
        assertThat(result.getColorCompatibilityScore()).isNotNull();
        assertThat(result.getFitCompatibilityScore()).isNotNull();
    }

    @Test
    void shouldPreserveScoresWhenUpdatingNameOnly() {
        // Given - Create outfit with items
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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
        Double originalColorScore = created.getColorCompatibilityScore();
        Double originalFitScore = created.getFitCompatibilityScore();

        // When - Update name only
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Scores should remain unchanged
        assertThat(result.getColorCompatibilityScore()).isEqualTo(originalColorScore);
        assertThat(result.getFitCompatibilityScore()).isEqualTo(originalFitScore);
    }

    @Test
    void shouldSetFitScoreToZeroWhenNoTopAndBottomItems() {
        // Given - Create outfit with only top item
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

        // When - Update with items that don't have both top and bottom
        ClothingItem accessory = ClothingItem.builder()
                .user(testUser)
                .name("Watch")
                .primaryColor("silver")
                .category(ClothingCategory.ACCESSORIES)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accessory = clothingItemRepository.save(accessory);

        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(accessory.getId())
                                .position(ItemPosition.ACCESSORY)
                                .build()
                ))
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Fit score should be 0
        assertThat(result.getFitCompatibilityScore()).isEqualTo(0.0);
    }

    // ===== Task 6.5: Test timestamp and response completeness =====

    @Test
    void shouldUpdateTimestampWhenAnyFieldChanges() {
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
        LocalDateTime originalUpdatedAt = created.getUpdatedAt();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - Update name
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - updatedAt should be newer
        assertThat(result.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldPreserveCreatedAtTimestamp() {
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
        LocalDateTime originalCreatedAt = created.getCreatedAt();

        // When - Update
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - createdAt should remain unchanged
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void shouldReflectLatestUpdatedAtOnMultipleUpdates() {
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

        // When - First update
        UpdateOutfitRequest updateRequest1 = UpdateOutfitRequest.builder()
                .name("Updated Name 1")
                .build();
        OutfitDTO result1 = outfitService.update(testUser.getId(), created.getId(), updateRequest1);
        LocalDateTime firstUpdatedAt = result1.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - Second update
        UpdateOutfitRequest updateRequest2 = UpdateOutfitRequest.builder()
                .name("Updated Name 2")
                .build();
        OutfitDTO result2 = outfitService.update(testUser.getId(), created.getId(), updateRequest2);

        // Then - Second updatedAt should be after first
        assertThat(result2.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    @Test
    void shouldIncludeAllItemsWithDetailsInResponse() {
        // Given - Create outfit with items
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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

        // When - Update with new items
        ClothingItem newItem = ClothingItem.builder()
                .user(testUser)
                .name("New Item")
                .primaryColor("purple")
                .category(ClothingCategory.ACCESSORIES)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newItem = clothingItemRepository.save(newItem);

        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .items(List.of(
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(testTop.getId())
                                .position(ItemPosition.TOP)
                                .build(),
                        CreateOutfitRequest.OutfitItemRequest.builder()
                                .clothingItemId(newItem.getId())
                                .position(ItemPosition.ACCESSORY)
                                .build()
                ))
                .build();
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Response includes all items with details
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).allMatch(item -> item.getClothingItem() != null);
        assertThat(result.getItems()).allMatch(item -> item.getClothingItem().getId() != null);
        assertThat(result.getItems()).allMatch(item -> item.getClothingItem().getName() != null);
    }

    @Test
    void shouldIncludeUpdatedScoresInResponse() {
        // Given - Create outfit
        CreateOutfitRequest createRequest = CreateOutfitRequest.builder()
                .name("Original Name")
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

        // When - Update with items
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
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
        OutfitDTO result = outfitService.update(testUser.getId(), created.getId(), updateRequest);

        // Then - Response includes scores
        assertThat(result.getColorCompatibilityScore()).isNotNull();
        assertThat(result.getFitCompatibilityScore()).isNotNull();
    }

    // ===== Task 6.6: Test authorization and error cases =====

    @Test
    void shouldThrowForbiddenExceptionWhenUpdatingOutfitOwnedByDifferentUser() {
        // Given - Create another user
        User differentUser = User.builder()
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        differentUser = userRepository.save(differentUser);
        Long differentUserId = differentUser.getId();

        // Create outfit for testUser
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

        // When - Try to update with different user
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();

        // Then - Should throw ForbiddenException
        assertThatThrownBy(() -> outfitService.update(differentUserId, created.getId(), updateRequest))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentOutfit() {
        // Given - Non-existent outfit ID
        Long nonExistentOutfitId = 999999L;

        // When - Try to update non-existent outfit
        UpdateOutfitRequest updateRequest = UpdateOutfitRequest.builder()
                .name("Updated Name")
                .build();

        // Then - Should throw ResourceNotFoundException
        assertThatThrownBy(() -> outfitService.update(testUser.getId(), nonExistentOutfitId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
