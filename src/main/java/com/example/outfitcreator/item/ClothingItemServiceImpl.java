package com.example.outfitcreator.item;

import com.example.outfitcreator.entity.AuditLog;
import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.exception.ForbiddenException;
import com.example.outfitcreator.exception.ResourceNotFoundException;
import com.example.outfitcreator.exception.ValidationException;
import com.example.outfitcreator.item.dto.*;
import com.example.outfitcreator.outfit.OutfitService;
import com.example.outfitcreator.photo.PhotoService;
import com.example.outfitcreator.repository.AuditLogRepository;
import com.example.outfitcreator.repository.ClothingItemRepository;
import com.example.outfitcreator.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClothingItemServiceImpl implements ClothingItemService {

    private static final Logger log = LoggerFactory.getLogger(ClothingItemServiceImpl.class);

    private final ClothingItemRepository clothingItemRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PhotoService photoService;
    private final OutfitService outfitService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ClothingItemServiceImpl(ClothingItemRepository clothingItemRepository,
                                   UserRepository userRepository,
                                   AuditLogRepository auditLogRepository,
                                   PhotoService photoService,
                                   OutfitService outfitService) {
        this.clothingItemRepository = clothingItemRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.photoService = photoService;
        this.outfitService = outfitService;
    }

    @Override
    @Transactional
    public ClothingItemDTO create(Long userId, CreateClothingItemRequest request, MultipartFile photo) {
        log.info("Creating clothing item for user {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate category enum
        if (request.getCategory() == null) {
            throw new ValidationException("Invalid category", Map.of("category", "Category is required"));
        }

        // Create clothing item entity
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
                .wearCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save to get ID
        item = clothingItemRepository.save(item);

        // Upload photo if provided
        if (photo != null && !photo.isEmpty()) {
            try {
                log.info("Uploading photo for item {}, original filename: {}, size: {} bytes", 
                        item.getId(), photo.getOriginalFilename(), photo.getSize());
                String photoPath = photoService.uploadPhoto(photo, item.getId());
                log.info("Photo uploaded successfully to: {}", photoPath);
                item.setPhotoPath(photoPath);
                item = clothingItemRepository.save(item);
                log.info("Item {} updated with photo path", item.getId());
            } catch (IOException e) {
                log.error("Failed to upload photo for item {}", item.getId(), e);
                throw new RuntimeException("Failed to upload photo", e);
            }
        } else {
            log.info("No photo provided for item {}", item.getId());
        }

        // Create audit log
        createAuditLog(userId, "ClothingItem", item.getId(), "CREATE", 
                String.format("Created clothing item: %s", item.getName()));

        log.info("Created clothing item {} for user {}", item.getId(), userId);
        return toDTO(item);
    }

    @Override
    @Transactional
    public ClothingItemDTO update(Long userId, Long itemId, UpdateClothingItemRequest request) {
        log.info("Updating clothing item {} for user {}", itemId, userId);

        // Find item and verify ownership
        ClothingItem item = clothingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        // Validate category enum
        if (request.getCategory() == null) {
            throw new ValidationException("Invalid category", Map.of("category", "Category is required"));
        }

        // Store old values for audit
        String oldValues = String.format("name=%s, category=%s, color=%s", 
                item.getName(), item.getCategory(), item.getPrimaryColor());

        // Check if attributes that affect compatibility scores are changing
        boolean scoresNeedRecalculation = 
                !item.getPrimaryColor().equals(request.getPrimaryColor()) ||
                (item.getSecondaryColor() != null && !item.getSecondaryColor().equals(request.getSecondaryColor())) ||
                item.getFitCategory() != request.getFitCategory();

        // Update attributes
        item.setName(request.getName());
        item.setBrand(request.getBrand());
        item.setPrimaryColor(request.getPrimaryColor());
        item.setSecondaryColor(request.getSecondaryColor());
        item.setCategory(request.getCategory());
        item.setSize(request.getSize());
        item.setSeason(request.getSeason());
        item.setFitCategory(request.getFitCategory());
        item.setPurchaseDate(request.getPurchaseDate());
        item.setUpdatedAt(LocalDateTime.now());

        item = clothingItemRepository.save(item);

        // Create audit log
        String newValues = String.format("name=%s, category=%s, color=%s", 
                item.getName(), item.getCategory(), item.getPrimaryColor());
        createAuditLog(userId, "ClothingItem", item.getId(), "UPDATE", 
                String.format("Updated: %s -> %s", oldValues, newValues));

        // Trigger outfit score recalculation if needed
        if (scoresNeedRecalculation) {
            log.info("Triggering outfit score recalculation for item {}", itemId);
            outfitService.recalculateScoresForItem(itemId);
        }

        log.info("Updated clothing item {} for user {}", itemId, userId);
        return toDTO(item);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long itemId) {
        log.info("Deleting clothing item {} for user {}", itemId, userId);

        // Find item and verify ownership
        ClothingItem item = clothingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        // Check for outfit references
        boolean existsInOutfits = clothingItemRepository.existsInOutfits(itemId);
        if (existsInOutfits) {
            log.warn("Clothing item {} is referenced in outfits", itemId);
            // Handle outfit item deletion by marking outfits as incomplete
            outfitService.handleClothingItemDeletion(itemId);
        }

        // Delete photo if exists
        if (item.getPhotoPath() != null) {
            photoService.deletePhoto(item.getPhotoPath());
        }

        // Create audit log before deletion
        createAuditLog(userId, "ClothingItem", item.getId(), "DELETE", 
                String.format("Deleted clothing item: %s", item.getName()));

        // Delete item
        clothingItemRepository.delete(item);

        log.info("Deleted clothing item {} for user {}", itemId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothingItemDTO getById(Long userId, Long itemId) {
        log.debug("Getting clothing item {} for user {}", itemId, userId);

        ClothingItem item = clothingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found"));

        // Verify ownership
        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        return toDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClothingItemDTO> findAll(Long userId, ClothingItemFilter filter, Pageable pageable) {
        log.debug("Finding clothing items for user {} with filter {}", userId, filter);

        Specification<ClothingItem> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            // Apply filters
            if (filter != null) {
                if (filter.getCategory() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
                }
                if (filter.getSeason() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("season"), filter.getSeason()));
                }
                if (filter.getColor() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("primaryColor"), filter.getColor()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return clothingItemRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public ClothingItemDTO uploadPhoto(Long userId, Long itemId, MultipartFile photo) {
        log.info("Uploading photo for clothing item {} for user {}", itemId, userId);

        // Find item and verify ownership
        ClothingItem item = clothingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        // Delete old photo if exists
        if (item.getPhotoPath() != null) {
            photoService.deletePhoto(item.getPhotoPath());
        }

        // Upload new photo
        try {
            String photoPath = photoService.uploadPhoto(photo, item.getId());
            item.setPhotoPath(photoPath);
            item.setUpdatedAt(LocalDateTime.now());
            item = clothingItemRepository.save(item);

            // Create audit log
            createAuditLog(userId, "ClothingItem", item.getId(), "UPDATE", 
                    "Updated photo");

            log.info("Uploaded photo for clothing item {} for user {}", itemId, userId);
            return toDTO(item);
        } catch (IOException e) {
            log.error("Failed to upload photo for item {}", itemId, e);
            throw new RuntimeException("Failed to upload photo", e);
        }
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
                .photoUrl(generatePhotoUrl(item.getPhotoPath()))
                .wearCount(item.getWearCount())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private String generatePhotoUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }
        // Extract filename from path (handle both / and \ separators)
        String filename = photoPath.replace("\\", "/").substring(photoPath.replace("\\", "/").lastIndexOf('/') + 1);
        return String.format("%s/api/photos/%s", baseUrl, filename);
    }

    private void createAuditLog(Long userId, String entityType, Long entityId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId.toString())
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}
