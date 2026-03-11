package com.example.outfitcreator.outfit;

import com.example.outfitcreator.outfit.dto.CreateOutfitRequest;
import com.example.outfitcreator.outfit.dto.OutfitDTO;
import com.example.outfitcreator.outfit.dto.UpdateOutfitRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for outfit management.
 */
public interface OutfitService {
    
    /**
     * Create a new outfit with clothing items.
     */
    OutfitDTO create(Long userId, CreateOutfitRequest request);
    
    /**
     * Update outfit name and notes.
     */
    OutfitDTO update(Long userId, Long outfitId, UpdateOutfitRequest request);
    
    /**
     * Delete an outfit.
     */
    void delete(Long userId, Long outfitId);
    
    /**
     * Get outfit by ID with user ownership verification.
     */
    OutfitDTO getById(Long userId, Long outfitId);
    
    /**
     * Find all outfits for a user with pagination.
     */
    Page<OutfitDTO> findAll(Long userId, Pageable pageable);
    
    /**
     * Handle clothing item deletion by marking affected outfits as incomplete.
     */
    void handleClothingItemDeletion(Long itemId);
    
    /**
     * Recalculate compatibility scores for outfits containing the specified clothing item.
     */
    void recalculateScoresForItem(Long itemId);
}
