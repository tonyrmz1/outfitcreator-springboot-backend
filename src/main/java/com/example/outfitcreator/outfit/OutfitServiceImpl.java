package com.example.outfitcreator.outfit;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.Outfit;
import com.example.outfitcreator.entity.OutfitItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.exception.ForbiddenException;
import com.example.outfitcreator.exception.ResourceNotFoundException;
import com.example.outfitcreator.exception.ValidationException;
import com.example.outfitcreator.item.dto.ClothingItemDTO;
import com.example.outfitcreator.outfit.dto.CreateOutfitRequest;
import com.example.outfitcreator.outfit.dto.OutfitDTO;
import com.example.outfitcreator.outfit.dto.OutfitItemDTO;
import com.example.outfitcreator.outfit.dto.UpdateOutfitRequest;
import com.example.outfitcreator.recommendation.RecommendationEngine;
import com.example.outfitcreator.repository.ClothingItemRepository;
import com.example.outfitcreator.repository.OutfitRepository;
import com.example.outfitcreator.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OutfitServiceImpl implements OutfitService {

    private static final Logger log = LoggerFactory.getLogger(OutfitServiceImpl.class);

    private final OutfitRepository outfitRepository;
    private final ClothingItemRepository clothingItemRepository;
    private final UserRepository userRepository;
    private final RecommendationEngine recommendationEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public OutfitServiceImpl(OutfitRepository outfitRepository,
                            ClothingItemRepository clothingItemRepository,
                            UserRepository userRepository,
                            RecommendationEngine recommendationEngine) {
        this.outfitRepository = outfitRepository;
        this.clothingItemRepository = clothingItemRepository;
        this.userRepository = userRepository;
        this.recommendationEngine = recommendationEngine;
    }

    @Override
    @Transactional
    public OutfitDTO create(Long userId, CreateOutfitRequest request) {
        log.info("Creating outfit for user {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate all clothing items exist and belong to the user
        List<ClothingItem> clothingItems = new ArrayList<>();
        for (CreateOutfitRequest.OutfitItemRequest itemRequest : request.getItems()) {
            ClothingItem item = clothingItemRepository.findById(itemRequest.getClothingItemId())
                    .orElseThrow(() -> new ValidationException(
                            "Clothing item not found",
                            Map.of("clothingItemId", "Item with ID " + itemRequest.getClothingItemId() + " not found")));

            // Verify ownership
            if (!item.getUser().getId().equals(userId)) {
                throw new ForbiddenException("Cannot add clothing items from other users");
            }

            clothingItems.add(item);
        }

        // Create outfit entity
        Outfit outfit = Outfit.builder()
                .user(user)
                .name(request.getName())
                .notes(request.getNotes())
                .isComplete(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        // Save outfit first to get ID
        outfit = outfitRepository.save(outfit);

        // Create outfit items
        for (int i = 0; i < request.getItems().size(); i++) {
            CreateOutfitRequest.OutfitItemRequest itemRequest = request.getItems().get(i);
            ClothingItem clothingItem = clothingItems.get(i);

            OutfitItem outfitItem = OutfitItem.builder()
                    .outfit(outfit)
                    .clothingItem(clothingItem)
                    .position(itemRequest.getPosition())
                    .build();

            outfit.getItems().add(outfitItem);
        }

        // Save with items
        outfit = outfitRepository.save(outfit);

        log.info("Created outfit {} for user {}", outfit.getId(), userId);
        return toDTO(outfit);
    }

    @Override
    @Transactional
    public OutfitDTO update(Long userId, Long outfitId, UpdateOutfitRequest request) {
        log.info("Updating outfit {} for user {}", outfitId, userId);

        // Find outfit and verify ownership
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found"));

        if (!outfit.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        // Validate items if provided (before any database changes)
        List<ClothingItem> validatedItems = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateOutfitRequest.OutfitItemRequest itemRequest : request.getItems()) {
                // Check if clothing item exists
                ClothingItem clothingItem = clothingItemRepository.findById(itemRequest.getClothingItemId())
                        .orElseThrow(() -> new ValidationException(
                                "Clothing item not found with ID: " + itemRequest.getClothingItemId(),
                                Map.of("clothingItemId", "Item not found: " + itemRequest.getClothingItemId())));

                // Verify item belongs to the authenticated user
                if (!clothingItem.getUser().getId().equals(userId)) {
                    throw new ForbiddenException("Access denied");
                }

                validatedItems.add(clothingItem);
            }
        }

        // Update name and notes
        if (request.getName() != null) {
            outfit.setName(request.getName());
        }
        if (request.getNotes() != null) {
            outfit.setNotes(request.getNotes());
        }

        // Replace outfit items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Clear existing outfit items
            outfit.getItems().clear();

            // Add new outfit items from validated items and request positions
            for (int i = 0; i < validatedItems.size(); i++) {
                CreateOutfitRequest.OutfitItemRequest itemRequest = request.getItems().get(i);
                ClothingItem clothingItem = validatedItems.get(i);

                OutfitItem outfitItem = OutfitItem.builder()
                        .outfit(outfit)
                        .clothingItem(clothingItem)
                        .position(itemRequest.getPosition())
                        .build();

                outfit.getItems().add(outfitItem);
            }

            // Recalculate compatibility scores when items are updated
            Map<String, Double> scores = calculateCompatibilityScores(outfit);
            outfit.setColorCompatibilityScore(scores.get("color"));
            outfit.setFitCompatibilityScore(scores.get("fit"));
        }

        outfit.setUpdatedAt(LocalDateTime.now());

        outfit = outfitRepository.save(outfit);

        log.info("Updated outfit {} for user {}", outfitId, userId);
        return toDTO(outfit);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long outfitId) {
        log.info("Deleting outfit {} for user {}", outfitId, userId);

        // Find outfit and verify ownership
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found"));

        if (!outfit.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        // Delete outfit (cascade will handle outfit items)
        outfitRepository.delete(outfit);

        log.info("Deleted outfit {} for user {}", outfitId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public OutfitDTO getById(Long userId, Long outfitId) {
        log.debug("Getting outfit {} for user {}", outfitId, userId);

        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found"));

        // Verify ownership
        if (!outfit.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        return toDTO(outfit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutfitDTO> findAll(Long userId, Pageable pageable) {
        log.debug("Finding all outfits for user {}", userId);

        return outfitRepository.findByUserId(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public void handleClothingItemDeletion(Long itemId) {
        log.info("Handling clothing item deletion for item {}", itemId);

        // Find all outfits containing this item
        List<Outfit> affectedOutfits = outfitRepository.findAll().stream()
                .filter(outfit -> outfit.getItems().stream()
                        .anyMatch(outfitItem -> outfitItem.getClothingItem() != null 
                                && outfitItem.getClothingItem().getId().equals(itemId)))
                .collect(Collectors.toList());

        // Mark affected outfits as incomplete
        for (Outfit outfit : affectedOutfits) {
            outfit.setIsComplete(false);
            outfit.setUpdatedAt(LocalDateTime.now());
            outfitRepository.save(outfit);
            log.info("Marked outfit {} as incomplete due to item {} deletion", outfit.getId(), itemId);
        }
    }

    private OutfitDTO toDTO(Outfit outfit) {
        List<OutfitItemDTO> itemDTOs = outfit.getItems().stream()
                .map(this::toOutfitItemDTO)
                .collect(Collectors.toList());

        // Calculate compatibility scores on-demand if not already stored
        Double colorScore = outfit.getColorCompatibilityScore();
        Double fitScore = outfit.getFitCompatibilityScore();
        
        if (colorScore == null || fitScore == null) {
            Map<String, Double> scores = calculateCompatibilityScores(outfit);
            colorScore = scores.get("color");
            fitScore = scores.get("fit");
        }

        return OutfitDTO.builder()
                .id(outfit.getId())
                .name(outfit.getName())
                .notes(outfit.getNotes())
                .items(itemDTOs)
                .isComplete(outfit.getIsComplete())
                .colorCompatibilityScore(colorScore)
                .fitCompatibilityScore(fitScore)
                .createdAt(outfit.getCreatedAt())
                .updatedAt(outfit.getUpdatedAt())
                .build();
    }

    private OutfitItemDTO toOutfitItemDTO(OutfitItem outfitItem) {
        ClothingItemDTO clothingItemDTO = null;
        if (outfitItem.getClothingItem() != null) {
            clothingItemDTO = toClothingItemDTO(outfitItem.getClothingItem());
        }

        return OutfitItemDTO.builder()
                .id(outfitItem.getId())
                .clothingItem(clothingItemDTO)
                .position(outfitItem.getPosition())
                .build();
    }

    private ClothingItemDTO toClothingItemDTO(ClothingItem item) {
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
        // Extract filename from path
        String filename = photoPath.substring(photoPath.lastIndexOf('/') + 1);
        return String.format("%s/api/photos/%s", baseUrl, filename);
    }

    /**
     * Calculate compatibility scores for an outfit.
     * Returns a map with "color" and "fit" scores.
     */
    private Map<String, Double> calculateCompatibilityScores(Outfit outfit) {
        List<ClothingItem> items = outfit.getItems().stream()
                .map(OutfitItem::getClothingItem)
                .filter(item -> item != null)
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            return Map.of("color", 0.0, "fit", 0.0);
        }

        // Find top and bottom items for fit compatibility
        ClothingItem top = items.stream()
                .filter(item -> item.getCategory() == ClothingCategory.TOP)
                .findFirst()
                .orElse(null);

        ClothingItem bottom = items.stream()
                .filter(item -> item.getCategory() == ClothingCategory.BOTTOM)
                .findFirst()
                .orElse(null);

        // Calculate color compatibility (average of all pairwise combinations)
        double totalColorScore = 0.0;
        int colorPairs = 0;
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                totalColorScore += recommendationEngine.calculateColorCompatibility(items.get(i), items.get(j));
                colorPairs++;
            }
        }
        double colorScore = colorPairs > 0 ? totalColorScore / colorPairs : 0.0;

        // Calculate fit compatibility (only if both top and bottom exist)
        double fitScore = 0.0;
        if (top != null && bottom != null) {
            fitScore = recommendationEngine.calculateFitCompatibility(top, bottom);
        }

        return Map.of("color", colorScore, "fit", fitScore);
    }

    @Override
    @Transactional
    public void recalculateScoresForItem(Long itemId) {
        log.info("Recalculating compatibility scores for outfits containing item {}", itemId);

        // Find all outfits containing this item
        List<Outfit> affectedOutfits = outfitRepository.findAll().stream()
                .filter(outfit -> outfit.getItems().stream()
                        .anyMatch(outfitItem -> outfitItem.getClothingItem() != null 
                                && outfitItem.getClothingItem().getId().equals(itemId)))
                .collect(Collectors.toList());

        // Recalculate scores for each affected outfit
        for (Outfit outfit : affectedOutfits) {
            Map<String, Double> scores = calculateCompatibilityScores(outfit);
            outfit.setColorCompatibilityScore(scores.get("color"));
            outfit.setFitCompatibilityScore(scores.get("fit"));
            outfit.setUpdatedAt(LocalDateTime.now());
            outfitRepository.save(outfit);
            log.info("Updated compatibility scores for outfit {}: color={}, fit={}", 
                    outfit.getId(), scores.get("color"), scores.get("fit"));
        }

        log.info("Recalculated scores for {} outfits", affectedOutfits.size());
    }
}
