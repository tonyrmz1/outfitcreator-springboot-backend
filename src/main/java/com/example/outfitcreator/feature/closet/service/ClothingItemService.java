package com.example.outfitcreator.feature.closet.service;

import com.example.outfitcreator.feature.closet.dto.request.CreateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.request.UpdateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ClothingItemService {

    ClothingItemDTO getItem(Long userId, Long id);

    Page<ClothingItemDTO> getAll(Long userId, ClothingItemFilter filter, Pageable pageable);

    ClothingItemDTO createItem(Long userId, CreateClothingItemRequest request, MultipartFile photo) throws IOException;

    ClothingItemDTO updateItem(Long userId, Long id, UpdateClothingItemRequest request);

    void deleteItem(Long userId, Long id);

    ClothingItemDTO uploadPhoto(Long userId, Long id, MultipartFile photo) throws IOException;
}
