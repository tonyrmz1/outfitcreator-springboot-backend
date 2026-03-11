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
}
