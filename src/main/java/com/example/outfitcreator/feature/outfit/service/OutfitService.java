package com.example.outfitcreator.feature.outfit.service;

import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OutfitService {
    
    OutfitDTO create(Long userId, CreateOutfitRequest request);
    
    OutfitDTO update(Long userId, Long outfitId, UpdateOutfitRequest request);
    
    void delete(Long userId, Long outfitId);
    
    OutfitDTO getById(Long userId, Long outfitId);
    
    Page<OutfitDTO> findAll(Long userId, Pageable pageable);
    
    void handleClothingItemDeletion(Long itemId);
    
    void recalculateScoresForItem(Long itemId);
}
