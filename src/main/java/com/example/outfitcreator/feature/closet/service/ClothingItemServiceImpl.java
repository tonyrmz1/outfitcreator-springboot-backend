package com.example.outfitcreator.feature.closet.service;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import com.example.outfitcreator.feature.closet.dto.request.CreateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.request.UpdateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import com.example.outfitcreator.feature.photo.service.PhotoService;
import com.example.outfitcreator.feature.photo.service.PhotoUrlService;
import com.example.outfitcreator.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ClothingItemServiceImpl implements ClothingItemService {

    private static final Logger log = LoggerFactory.getLogger(ClothingItemServiceImpl.class);

    private final ClothingItemRepository clothingItemRepository;
    private final UserRepository userRepository;
    private final PhotoService photoService;
    private final PhotoUrlService photoUrlService;

    public ClothingItemServiceImpl(ClothingItemRepository clothingItemRepository,
                                   UserRepository userRepository,
                                   PhotoService photoService,
                                   PhotoUrlService photoUrlService) {
        this.clothingItemRepository = clothingItemRepository;
        this.userRepository = userRepository;
        this.photoService = photoService;
        this.photoUrlService = photoUrlService;
    }

    @Override
    public ClothingItemDTO getItem(Long userId, Long id) {
        ClothingItem item = clothingItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found with id: " + id));
        return toDTO(item);
    }

    @Override
    public Page<ClothingItemDTO> getAll(Long userId, Pageable pageable,
                                        ClothingCategory category, Season season,
                                        String color, String searchQuery) {
        Specification<ClothingItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (season != null) {
                predicates.add(cb.equal(root.get("season"), season));
            }
            if (color != null && !color.isBlank()) {
                String pattern = "%" + color.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("primaryColor")), pattern));
            }
            if (searchQuery != null && !searchQuery.isBlank()) {
                String pattern = "%" + searchQuery.toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate byBrand = cb.like(cb.lower(root.get("brand")), pattern);
                predicates.add(cb.or(byName, byBrand));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return clothingItemRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public ClothingItemDTO createItem(Long userId, CreateClothingItemRequest request, MultipartFile photo) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ClothingItem item = ClothingItem.builder()
                .user(user)
                .name(request.getName())
                .brand(request.getBrand())
                .primaryColor(request.getPrimaryColor())
                .secondaryColor(request.getSecondaryColor())
                .category(request.getCategory())
                .size(request.getSize())
                .season(request.getSeason())
                .fitCategory(request.getFitCategory())
                .purchaseDate(request.getPurchaseDate())
                .build();

        ClothingItem saved = clothingItemRepository.save(item);

        if (photo != null && !photo.isEmpty()) {
            String photoPath = photoService.uploadPhoto(photo, saved.getId());
            saved.setPhotoPath(photoPath);
            saved = clothingItemRepository.save(saved);
        }

        log.info("Created clothing item {} for user {}", saved.getId(), userId);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public ClothingItemDTO updateItem(Long userId, Long id, UpdateClothingItemRequest request) {
        ClothingItem item = clothingItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found with id: " + id));

        item.setName(request.getName());
        item.setBrand(request.getBrand());
        item.setPrimaryColor(request.getPrimaryColor());
        item.setSecondaryColor(request.getSecondaryColor());
        item.setCategory(request.getCategory());
        item.setSize(request.getSize());
        item.setSeason(request.getSeason());
        item.setFitCategory(request.getFitCategory());
        item.setPurchaseDate(request.getPurchaseDate());

        ClothingItem updated = clothingItemRepository.save(item);
        log.info("Updated clothing item {} for user {}", id, userId);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long id) {
        ClothingItem item = clothingItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found with id: " + id));

        if (item.getPhotoPath() != null) {
            photoService.deletePhoto(item.getPhotoPath());
        }

        clothingItemRepository.delete(item);
        log.info("Deleted clothing item {} for user {}", id, userId);
    }

    @Override
    @Transactional
    public ClothingItemDTO uploadPhoto(Long userId, Long id, MultipartFile photo) throws IOException {
        ClothingItem item = clothingItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found with id: " + id));

        if (item.getPhotoPath() != null) {
            photoService.deletePhoto(item.getPhotoPath());
        }

        String photoPath = photoService.uploadPhoto(photo, id);
        item.setPhotoPath(photoPath);
        ClothingItem updated = clothingItemRepository.save(item);

        log.info("Uploaded photo for clothing item {} for user {}", id, userId);
        return toDTO(updated);
    }

    private ClothingItemDTO toDTO(ClothingItem item) {
        return ClothingItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .primaryColor(item.getPrimaryColor())
                .secondaryColor(item.getSecondaryColor())
                .category(item.getCategory())
                .size(item.getSize())
                .season(item.getSeason())
                .fitCategory(item.getFitCategory())
                .purchaseDate(item.getPurchaseDate())
                .photoUrl(photoUrlService.generatePhotoUrl(item.getPhotoPath()))
                .wearCount(item.getWearCount())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
