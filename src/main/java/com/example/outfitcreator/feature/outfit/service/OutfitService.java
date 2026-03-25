package com.example.outfitcreator.feature.outfit.service;

import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Application service for {@link com.example.outfitcreator.core.entity.Outfit} lifecycle and consistency with closet changes.
 * @author Antonio Marin Belmonte
 */
public interface OutfitService {

    /**
     * Creates an outfit from the user's items with unique positions per slot.
     */
    OutfitDTO create(Long userId, CreateOutfitRequest request);

    /**
     * Partial update of name, notes, and optionally replaces the full item list.
     */
    OutfitDTO update(Long userId, Long outfitId, UpdateOutfitRequest request);

    void delete(Long userId, Long outfitId);

    OutfitDTO getById(Long userId, Long outfitId);

    Page<OutfitDTO> findAll(Long userId, Pageable pageable);

    /**
     * Removes references when a clothing item is deleted from the closet.
     */
    void handleClothingItemDeletion(Long itemId);

    /**
     * Refreshes stored compatibility scores for outfits that include the given item.
     */
    void recalculateScoresForItem(Long itemId);
}
