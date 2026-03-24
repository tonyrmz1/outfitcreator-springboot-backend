package com.example.outfitcreator.feature.closet.service;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.closet.dto.request.CreateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.request.UpdateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Application service for managing a user's {@link com.example.outfitcreator.core.entity.ClothingItem} records and photos.
 */
public interface ClothingItemService {

    /**
     * Loads one item scoped to the user.
     */
    ClothingItemDTO getItem(Long userId, Long id);

    /**
     * Page of items for the user, optionally filtered by category, season, color substring, and name/brand search.
     */
    Page<ClothingItemDTO> getAll(Long userId, Pageable pageable,
                                 ClothingCategory category, Season season,
                                 String color, String searchQuery);

    /**
     * Persists a new item and optionally stores an uploaded photo.
     */
    ClothingItemDTO createItem(Long userId, CreateClothingItemRequest request, MultipartFile photo) throws IOException;

    /**
     * Updates mutable fields of an existing item owned by the user.
     */
    ClothingItemDTO updateItem(Long userId, Long id, UpdateClothingItemRequest request);

    /**
     * Deletes the item and its stored image files when applicable.
     */
    void deleteItem(Long userId, Long id);

    /**
     * Replaces or sets the photo for an existing item.
     */
    ClothingItemDTO uploadPhoto(Long userId, Long id, MultipartFile photo) throws IOException;
}
