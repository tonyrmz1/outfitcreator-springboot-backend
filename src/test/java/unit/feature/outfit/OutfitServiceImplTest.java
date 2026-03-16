package unit.feature.outfit;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.Outfit;
import com.example.outfitcreator.core.entity.OutfitItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ItemPosition;
import com.example.outfitcreator.shared.exception.ForbiddenException;
import com.example.outfitcreator.shared.exception.ResourceNotFoundException;
import com.example.outfitcreator.shared.exception.ValidationException;
import com.example.outfitcreator.feature.outfit.service.OutfitService;
import com.example.outfitcreator.feature.outfit.service.OutfitServiceImpl;
import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;
import com.example.outfitcreator.feature.recommendation.service.RecommendationEngine;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import com.example.outfitcreator.feature.outfit.repository.FeatureOutfitRepository;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OutfitServiceImpl.
 * Tests outfit creation, validation, business logic, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class OutfitServiceImplTest {

    @Mock
    private FeatureOutfitRepository outfitRepository;

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecommendationEngine recommendationEngine;

    @InjectMocks
    private OutfitServiceImpl outfitService;

    private User testUser;
    private User anotherUser;
    private ClothingItem testTop;
    private ClothingItem testBottom;
    private ClothingItem testFootwear;
    private Outfit testOutfit;

    @BeforeEach
    void setUp() {
        testUser = OutfitTestFixtures.createTestUser(1L);
        anotherUser = OutfitTestFixtures.createAnotherUser(2L);
        testTop = OutfitTestFixtures.createTestTop(1L, testUser);
        testBottom = OutfitTestFixtures.createTestBottom(2L, testUser);
        testFootwear = OutfitTestFixtures.createTestFootwear(3L, testUser);
        testOutfit = OutfitTestFixtures.createTestOutfit(1L, testUser);
    }

    @Nested
    @DisplayName("Outfit Creation Tests")
    class CreateOutfitTests {

        @Test
        @DisplayName("Should create outfit with valid request")
        void shouldCreateOutfitWithValidRequest() {
            CreateOutfitRequest request = OutfitTestFixtures.createValidCreateRequest(1L, 2L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(testTop));
            when(clothingItemRepository.findById(2L)).thenReturn(Optional.of(testBottom));

            Outfit savedOutfit = Outfit.builder()
                    .id(1L)
                    .user(testUser)
                    .name("New Outfit")
                    .notes("Outfit notes")
                    .isComplete(true)
                    .items(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(outfitRepository.save(any(Outfit.class))).thenReturn(savedOutfit);

            when(recommendationEngine.calculateColorCompatibility(any(), any())).thenReturn(85.0);
            when(recommendationEngine.calculateFitCompatibility(any(), any())).thenReturn(90.0);

            OutfitDTO result = outfitService.create(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Outfit");
            assertThat(result.getIsComplete()).isTrue();
            verify(userRepository).findById(1L);
            verify(clothingItemRepository, times(2)).findById(anyLong());
            verify(outfitRepository, times(2)).save(any(Outfit.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            CreateOutfitRequest request = OutfitTestFixtures.createValidCreateRequest(1L, 2L);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> outfitService.create(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(999L);
            verify(outfitRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when clothing item not found")
        void shouldThrowExceptionWhenClothingItemNotFound() {
            CreateOutfitRequest request = OutfitTestFixtures.createValidCreateRequest(999L, 2L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> outfitService.create(1L, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Clothing item not found");

            verify(clothingItemRepository).findById(999L);
            verify(outfitRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when clothing item belongs to another user")
        void shouldThrowExceptionWhenItemBelongsToAnotherUser() {
            ClothingItem anotherUserItem = OutfitTestFixtures.createTestTop(10L, anotherUser);
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Test Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(10L)
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(10L)).thenReturn(Optional.of(anotherUserItem));

            assertThatThrownBy(() -> outfitService.create(1L, request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Cannot add clothing items from other users");

            verify(outfitRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create outfit with single item")
        void shouldCreateOutfitWithSingleItem() {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Single Item Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(testTop));

            Outfit savedOutfit = Outfit.builder()
                    .id(1L)
                    .user(testUser)
                    .name("Single Item Outfit")
                    .notes("Outfit notes")
                    .isComplete(true)
                    .items(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(outfitRepository.save(any(Outfit.class))).thenReturn(savedOutfit);

            OutfitDTO result = outfitService.create(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Single Item Outfit");
            verify(outfitRepository, times(2)).save(any(Outfit.class));
        }
    }

    @Nested
    @DisplayName("Outfit Retrieval Tests")
    class GetOutfitTests {

        @Test
        @DisplayName("Should get outfit by ID for owner")
        void shouldGetOutfitByIdForOwner() {
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));

            OutfitDTO result = outfitService.getById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(outfitRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when outfit not found")
        void shouldThrowExceptionWhenOutfitNotFound() {
            when(outfitRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> outfitService.getById(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Outfit not found");

            verify(outfitRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when accessing another user's outfit")
        void shouldThrowExceptionWhenAccessingAnotherUsersOutfit() {
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));

            assertThatThrownBy(() -> outfitService.getById(2L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Access denied");

            verify(outfitRepository).findById(1L);
        }

        @Test
        @DisplayName("Should find all outfits for user with pagination")
        void shouldFindAllOutfitsWithPagination() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Outfit> outfitPage = new PageImpl<>(List.of(testOutfit));
            when(outfitRepository.findByUserId(1L, pageable)).thenReturn(outfitPage);

            Page<OutfitDTO> result = outfitService.findAll(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
            verify(outfitRepository).findByUserId(1L, pageable);
        }

        @Test
        @DisplayName("Should return empty page when user has no outfits")
        void shouldReturnEmptyPageWhenNoOutfits() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Outfit> emptyPage = new PageImpl<>(List.of());
            when(outfitRepository.findByUserId(1L, pageable)).thenReturn(emptyPage);

            Page<OutfitDTO> result = outfitService.findAll(1L, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(outfitRepository).findByUserId(1L, pageable);
        }
    }

    @Nested
    @DisplayName("Outfit Update Tests")
    class UpdateOutfitTests {

        @Test
        @DisplayName("Should update outfit name and notes")
        void shouldUpdateOutfitNameAndNotes() {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .notes("Updated Notes")
                    .build();

            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));
            when(outfitRepository.save(any(Outfit.class))).thenReturn(testOutfit);

            OutfitDTO result = outfitService.update(1L, 1L, request);

            assertThat(result).isNotNull();
            verify(outfitRepository).findById(1L);
            verify(outfitRepository).save(any(Outfit.class));
        }

        @Test
        @DisplayName("Should throw exception when updating another user's outfit")
        void shouldThrowExceptionWhenUpdatingAnotherUsersOutfit() {
            UpdateOutfitRequest request = OutfitTestFixtures.createValidUpdateRequest();
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));

            assertThatThrownBy(() -> outfitService.update(2L, 1L, request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Access denied");

            verify(outfitRepository).findById(1L);
            verify(outfitRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when outfit not found for update")
        void shouldThrowExceptionWhenOutfitNotFoundForUpdate() {
            UpdateOutfitRequest request = OutfitTestFixtures.createValidUpdateRequest();
            when(outfitRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> outfitService.update(1L, 999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Outfit not found");

            verify(outfitRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Outfit Deletion Tests")
    class DeleteOutfitTests {

        @Test
        @DisplayName("Should delete outfit for owner")
        void shouldDeleteOutfitForOwner() {
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));
            doNothing().when(outfitRepository).delete(testOutfit);

            outfitService.delete(1L, 1L);

            verify(outfitRepository).findById(1L);
            verify(outfitRepository).delete(testOutfit);
        }

        @Test
        @DisplayName("Should throw exception when deleting another user's outfit")
        void shouldThrowExceptionWhenDeletingAnotherUsersOutfit() {
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));

            assertThatThrownBy(() -> outfitService.delete(2L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Access denied");

            verify(outfitRepository).findById(1L);
            verify(outfitRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when outfit not found for deletion")
        void shouldThrowExceptionWhenOutfitNotFoundForDeletion() {
            when(outfitRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> outfitService.delete(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Outfit not found");

            verify(outfitRepository).findById(999L);
            verify(outfitRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Clothing Item Deletion Handling Tests")
    class ClothingItemDeletionTests {

        @Test
        @DisplayName("Should mark outfit as incomplete when clothing item is deleted")
        void shouldMarkOutfitAsIncompleteWhenItemDeleted() {
            OutfitItem outfitItem = OutfitTestFixtures.createOutfitItem(1L, testOutfit, testTop, ItemPosition.TOP);
            testOutfit.getItems().add(outfitItem);

            when(outfitRepository.findAll()).thenReturn(List.of(testOutfit));
            when(outfitRepository.save(any(Outfit.class))).thenReturn(testOutfit);

            outfitService.handleClothingItemDeletion(1L);

            verify(outfitRepository).findAll();
            verify(outfitRepository).save(argThat(outfit -> !outfit.getIsComplete()));
        }

        @Test
        @DisplayName("Should not affect outfits not containing the deleted item")
        void shouldNotAffectOutfitsNotContainingDeletedItem() {
            Outfit otherOutfit = OutfitTestFixtures.createTestOutfit(2L, testUser);
            when(outfitRepository.findAll()).thenReturn(List.of(otherOutfit));

            outfitService.handleClothingItemDeletion(999L);

            verify(outfitRepository).findAll();
            verify(outfitRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Score Calculation Tests")
    class ScoreCalculationTests {

        @Test
        @DisplayName("Should calculate compatibility scores for outfit with items")
        void shouldCalculateCompatibilityScoresForOutfitWithItems() {
            OutfitItem outfitItem = OutfitTestFixtures.createOutfitItem(1L, testOutfit, testTop, ItemPosition.TOP);
            OutfitItem bottomItem = OutfitTestFixtures.createOutfitItem(2L, testOutfit, testBottom, ItemPosition.BOTTOM);
            testOutfit.getItems().add(outfitItem);
            testOutfit.getItems().add(bottomItem);

            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));
            when(recommendationEngine.calculateColorCompatibility(any(), any())).thenReturn(85.0);
            when(recommendationEngine.calculateFitCompatibility(any(), any())).thenReturn(90.0);

            OutfitDTO result = outfitService.getById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getColorCompatibilityScore()).isEqualTo(85.0);
            assertThat(result.getFitCompatibilityScore()).isEqualTo(90.0);
        }

        @Test
        @DisplayName("Should return zero scores for empty outfit")
        void shouldReturnZeroScoresForEmptyOutfit() {
            when(outfitRepository.findById(1L)).thenReturn(Optional.of(testOutfit));

            OutfitDTO result = outfitService.getById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getColorCompatibilityScore()).isEqualTo(0.0);
            assertThat(result.getFitCompatibilityScore()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Score Recalculation Tests")
    class ScoreRecalculationTests {

        @Test
        @DisplayName("Should recalculate scores for outfits containing item")
        void shouldRecalculateScoresForOutfitsContainingItem() {
            Outfit outfitWithItems = Outfit.builder()
                    .id(1L)
                    .user(testUser)
                    .name("Test Outfit")
                    .notes("Test notes")
                    .isComplete(true)
                    .items(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            OutfitItem outfitItem = new OutfitItem();
            outfitItem.setId(1L);
            outfitItem.setOutfit(outfitWithItems);
            outfitItem.setClothingItem(testTop);
            outfitItem.setPosition(ItemPosition.TOP);
            outfitWithItems.getItems().add(outfitItem);

            when(outfitRepository.findAll()).thenReturn(List.of(outfitWithItems));
            when(outfitRepository.save(any(Outfit.class))).thenAnswer(inv -> {
                Outfit saved = inv.getArgument(0);
                saved.setColorCompatibilityScore(75.0);
                saved.setFitCompatibilityScore(80.0);
                return saved;
            });

            outfitService.recalculateScoresForItem(1L);

            verify(outfitRepository).findAll();
            verify(outfitRepository).save(any(Outfit.class));
        }

        @Test
        @DisplayName("Should not recalculate scores for outfits not containing item")
        void shouldNotRecalculateScoresForOutfitsNotContainingItem() {
            when(outfitRepository.findAll()).thenReturn(List.of(testOutfit));

            outfitService.recalculateScoresForItem(999L);

            verify(outfitRepository).findAll();
            verify(outfitRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle outfit with multiple items of same category")
        void shouldHandleOutfitWithMultipleItemsOfSameCategory() {
            ClothingItem anotherTop = OutfitTestFixtures.createTestTop(10L, testUser);
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Multi-Top Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(10L)
                                    .position(ItemPosition.OUTERWEAR)
                                    .build()
                    ))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(testTop));
            when(clothingItemRepository.findById(10L)).thenReturn(Optional.of(anotherTop));

            Outfit savedOutfit = Outfit.builder()
                    .id(1L)
                    .user(testUser)
                    .name("Multi-Top Outfit")
                    .notes("Outfit notes")
                    .isComplete(true)
                    .items(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(outfitRepository.save(any(Outfit.class))).thenReturn(savedOutfit);

            OutfitDTO result = outfitService.create(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Multi-Top Outfit");
        }

        @Test
        @DisplayName("Should handle outfit with all item positions")
        void shouldHandleOutfitWithAllItemPositions() {
            ClothingItem outerwear = OutfitTestFixtures.createTestOuterwear(4L, testUser);
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Complete Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(2L)
                                    .position(ItemPosition.BOTTOM)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(3L)
                                    .position(ItemPosition.FOOTWEAR)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(4L)
                                    .position(ItemPosition.OUTERWEAR)
                                    .build()
                    ))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(testTop));
            when(clothingItemRepository.findById(2L)).thenReturn(Optional.of(testBottom));
            when(clothingItemRepository.findById(3L)).thenReturn(Optional.of(testFootwear));
            when(clothingItemRepository.findById(4L)).thenReturn(Optional.of(outerwear));

            Outfit savedOutfit = Outfit.builder()
                    .id(1L)
                    .user(testUser)
                    .name("Complete Outfit")
                    .notes("Outfit notes")
                    .isComplete(true)
                    .items(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(outfitRepository.save(any(Outfit.class))).thenReturn(savedOutfit);

            when(recommendationEngine.calculateColorCompatibility(any(), any())).thenReturn(90.0);
            when(recommendationEngine.calculateFitCompatibility(any(), any())).thenReturn(95.0);

            OutfitDTO result = outfitService.create(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Complete Outfit");
        }
    }
}