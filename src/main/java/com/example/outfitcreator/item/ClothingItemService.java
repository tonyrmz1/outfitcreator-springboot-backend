package com.example.outfitcreator.item;

import com.example.outfitcreator.item.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ClothingItemService {

    ClothingItemDTO create(Long userId, CreateClothingItemRequest request, MultipartFile photo);

    ClothingItemDTO update(Long userId, Long itemId, UpdateClothingItemRequest request);

    void delete(Long userId, Long itemId);

    ClothingItemDTO getById(Long userId, Long itemId);

    Page<ClothingItemDTO> findAll(Long userId, ClothingItemFilter filter, Pageable pageable);

    ClothingItemDTO uploadPhoto(Long userId, Long itemId, MultipartFile photo);
}
