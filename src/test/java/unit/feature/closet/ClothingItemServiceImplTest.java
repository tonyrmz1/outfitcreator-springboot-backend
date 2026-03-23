package unit.feature.closet;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.feature.closet.dto.request.CreateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.request.UpdateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemFilter;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import com.example.outfitcreator.feature.closet.service.ClothingItemServiceImpl;
import com.example.outfitcreator.feature.photo.service.PhotoService;
import com.example.outfitcreator.feature.photo.service.PhotoUrlService;
import com.example.outfitcreator.shared.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothingItemServiceImpl Tests")
class ClothingItemServiceImplTest {

    @Mock private ClothingItemRepository clothingItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private PhotoService photoService;
    @Mock private PhotoUrlService photoUrlService;

    @InjectMocks
    private ClothingItemServiceImpl clothingItemService;

    private User testUser;
    private ClothingItem testItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testItem = ClothingItem.builder()
                .id(10L)
                .user(testUser)
                .name("Blue Jeans")
                .primaryColor("blue")
                .category(ClothingCategory.BOTTOM)
                .season(Season.ALL_SEASON)
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getItem")
    class GetItemTests {

        @Test
        @DisplayName("Should return ClothingItemDTO for an item that belongs to the user")
        void shouldReturnDTOForExistingItem() {
            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));
            when(photoUrlService.generatePhotoUrl(any())).thenReturn(null);

            ClothingItemDTO result = clothingItemService.getItem(1L, 10L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Blue Jeans");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when item is not found or belongs to a different user")
        void shouldThrowWhenItemNotFoundOrBelongsToDifferentUser() {
            when(clothingItemRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clothingItemService.getItem(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Clothing item not found");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAllTests {

        @Test
        @DisplayName("Should return a paged result of ClothingItemDTOs for the user")
        void shouldReturnPagedResults() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ClothingItem> itemPage = new PageImpl<>(List.of(testItem), pageable, 1);

            when(clothingItemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
            when(photoUrlService.generatePhotoUrl(any())).thenReturn(null);

            Page<ClothingItemDTO> result = clothingItemService.getAll(1L, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Blue Jeans");
        }
    }

    @Nested
    @DisplayName("createItem")
    class CreateItemTests {

        @Test
        @DisplayName("Should create item and not call PhotoService when photo is null")
        void shouldCreateItemWithoutPhoto() throws IOException {
            CreateClothingItemRequest request = CreateClothingItemRequest.builder()
                    .name("White T-Shirt")
                    .primaryColor("white")
                    .category(ClothingCategory.TOP)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.save(any(ClothingItem.class))).thenAnswer(inv -> {
                ClothingItem item = inv.getArgument(0);
                item.setId(20L);
                return item;
            });
            when(photoUrlService.generatePhotoUrl(any())).thenReturn(null);

            ClothingItemDTO result = clothingItemService.createItem(1L, request, null);

            assertThat(result.getName()).isEqualTo("White T-Shirt");
            verify(photoService, never()).uploadPhoto(any(), anyLong());
            verify(clothingItemRepository, times(1)).save(any(ClothingItem.class));
        }

        @Test
        @DisplayName("Should create item and delegate photo upload when a photo file is provided")
        void shouldCreateItemWithPhoto() throws IOException {
            CreateClothingItemRequest request = CreateClothingItemRequest.builder()
                    .name("Red Dress")
                    .primaryColor("red")
                    .category(ClothingCategory.TOP)
                    .build();

            MockMultipartFile photo = new MockMultipartFile(
                    "photo", "dress.jpg", "image/jpeg", new byte[]{1, 2, 3});

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(clothingItemRepository.save(any(ClothingItem.class))).thenAnswer(inv -> {
                ClothingItem item = inv.getArgument(0);
                item.setId(30L);
                return item;
            });
            when(photoService.uploadPhoto(any(), anyLong())).thenReturn("photos/item_30.jpg");
            when(photoUrlService.generatePhotoUrl(any())).thenReturn("http://localhost/api/photos/item_30.jpg");

            ClothingItemDTO result = clothingItemService.createItem(1L, request, photo);

            assertThat(result.getName()).isEqualTo("Red Dress");
            verify(photoService).uploadPhoto(photo, 30L);
            verify(clothingItemRepository, times(2)).save(any(ClothingItem.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when the user does not exist")
        void shouldThrowWhenUserNotFound() {
            CreateClothingItemRequest request = CreateClothingItemRequest.builder()
                    .name("Blue Shirt")
                    .primaryColor("blue")
                    .category(ClothingCategory.TOP)
                    .build();

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clothingItemService.createItem(999L, request, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(clothingItemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItemTests {

        @Test
        @DisplayName("Should update fields and return updated ClothingItemDTO")
        void shouldUpdateItem() {
            UpdateClothingItemRequest request = UpdateClothingItemRequest.builder()
                    .name("Dark Jeans")
                    .primaryColor("dark blue")
                    .category(ClothingCategory.BOTTOM)
                    .build();

            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));
            when(clothingItemRepository.save(testItem)).thenReturn(testItem);
            when(photoUrlService.generatePhotoUrl(any())).thenReturn(null);

            ClothingItemDTO result = clothingItemService.updateItem(1L, 10L, request);

            assertThat(result.getName()).isEqualTo("Dark Jeans");
            verify(clothingItemRepository).save(testItem);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when item is not found or belongs to a different user")
        void shouldThrowWhenItemNotFoundOrBelongsToDifferentUser() {
            UpdateClothingItemRequest request = UpdateClothingItemRequest.builder()
                    .name("Dark Jeans")
                    .primaryColor("dark blue")
                    .category(ClothingCategory.BOTTOM)
                    .build();

            when(clothingItemRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clothingItemService.updateItem(1L, 99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Clothing item not found");
        }
    }

    @Nested
    @DisplayName("deleteItem")
    class DeleteItemTests {

        @Test
        @DisplayName("Should delete item and not call PhotoService when item has no photo")
        void shouldDeleteItemWithoutPhoto() {
            testItem.setPhotoPath(null);
            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));

            clothingItemService.deleteItem(1L, 10L);

            verify(photoService, never()).deletePhoto(any());
            verify(clothingItemRepository).delete(testItem);
        }

        @Test
        @DisplayName("Should delete item and its associated photo when photoPath is set")
        void shouldDeleteItemAndItsPhoto() {
            testItem.setPhotoPath("photos/item_10.jpg");
            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));

            clothingItemService.deleteItem(1L, 10L);

            verify(photoService).deletePhoto("photos/item_10.jpg");
            verify(clothingItemRepository).delete(testItem);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when item is not found or belongs to a different user")
        void shouldThrowWhenItemNotFoundOrBelongsToDifferentUser() {
            when(clothingItemRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clothingItemService.deleteItem(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Clothing item not found");

            verify(clothingItemRepository, never()).delete(any(ClothingItem.class));
        }
    }

    @Nested
    @DisplayName("uploadPhoto")
    class UploadPhotoTests {

        @Test
        @DisplayName("Should upload photo and update the item's photoPath")
        void shouldUploadPhoto() throws IOException {
            testItem.setPhotoPath(null);
            MockMultipartFile photo = new MockMultipartFile(
                    "photo", "shirt.jpg", "image/jpeg", new byte[]{1, 2, 3});

            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));
            when(photoService.uploadPhoto(photo, 10L)).thenReturn("photos/item_10.jpg");
            when(clothingItemRepository.save(testItem)).thenReturn(testItem);
            when(photoUrlService.generatePhotoUrl(any())).thenReturn("http://localhost/api/photos/item_10.jpg");

            ClothingItemDTO result = clothingItemService.uploadPhoto(1L, 10L, photo);

            assertThat(result).isNotNull();
            verify(photoService).uploadPhoto(photo, 10L);
            verify(photoService, never()).deletePhoto(any());
        }

        @Test
        @DisplayName("Should delete existing photo before uploading the new one")
        void shouldDeleteOldPhotoBeforeUploading() throws IOException {
            testItem.setPhotoPath("photos/old_photo.jpg");
            MockMultipartFile photo = new MockMultipartFile(
                    "photo", "new.jpg", "image/jpeg", new byte[]{1, 2, 3});

            when(clothingItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testItem));
            when(photoService.uploadPhoto(photo, 10L)).thenReturn("photos/new_photo.jpg");
            when(clothingItemRepository.save(testItem)).thenReturn(testItem);
            when(photoUrlService.generatePhotoUrl(any())).thenReturn("http://localhost/api/photos/new_photo.jpg");

            clothingItemService.uploadPhoto(1L, 10L, photo);

            verify(photoService).deletePhoto("photos/old_photo.jpg");
            verify(photoService).uploadPhoto(photo, 10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when item is not found")
        void shouldThrowWhenItemNotFound() {
            MockMultipartFile photo = new MockMultipartFile(
                    "photo", "shirt.jpg", "image/jpeg", new byte[]{1});
            when(clothingItemRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clothingItemService.uploadPhoto(1L, 99L, photo))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Clothing item not found");
        }
    }
}
