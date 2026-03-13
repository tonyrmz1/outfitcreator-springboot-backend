package com.example.outfitcreator.item;

import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.Season;
import com.example.outfitcreator.exception.ErrorResponse;
import com.example.outfitcreator.item.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothing")
@Tag(name = "Clothing Items", description = "Digital closet management - create, retrieve, update, and delete clothing items")
@SecurityRequirement(name = "bearerAuth")
public class ClothingItemController {

    private static final Logger log = LoggerFactory.getLogger(ClothingItemController.class);

    private final ClothingItemService clothingItemService;

    public ClothingItemController(ClothingItemService clothingItemService) {
        this.clothingItemService = clothingItemService;
    }

    @Operation(
            summary = "Create a new clothing item",
            description = "Creates a new clothing item in the user's digital closet with optional photo upload. " +
                    "Supports JPEG, PNG, and GIF formats up to 5MB. Images are automatically resized to max 1920x1080."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Clothing item created successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or unsupported file type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "File size exceeds 5MB limit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingItemDTO> createClothingItem(
            @Valid @ModelAttribute CreateClothingItemRequest data,
            @Parameter(description = "Photo file (JPEG, PNG, or GIF, max 5MB)")
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.info("Creating clothing item for user {}", userId);

        ClothingItemDTO created = clothingItemService.create(userId, data, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClothingItemDTO> createClothingItemJson(
            @Valid @RequestBody CreateClothingItemRequest request,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.info("Creating clothing item for user {}", userId);

        ClothingItemDTO created = clothingItemService.create(userId, request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Get all clothing items",
            description = "Retrieves all clothing items from the user's digital closet with optional filtering by category, season, and color. Supports pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clothing items retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<ClothingItemDTO>> getAllClothingItems(
            @Parameter(description = "Filter by clothing category (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORIES)")
            @RequestParam(required = false) ClothingCategory category,
            @Parameter(description = "Filter by season (SPRING, SUMMER, AUTUMN, WINTER, ALL_SEASON)")
            @RequestParam(required = false) Season season,
            @Parameter(description = "Filter by primary color")
            @RequestParam(required = false) String color,
            @Parameter(description = "Search by name or brand")
            @RequestParam(required = false) String searchQuery,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.debug("Getting clothing items for user {} with filters: category={}, season={}, color={}, searchQuery={}", 
                userId, category, season, color, searchQuery);

        ClothingItemFilter filter = ClothingItemFilter.builder()
                .category(category)
                .season(season)
                .color(color)
                .searchQuery(searchQuery)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<ClothingItemDTO> items = clothingItemService.findAll(userId, filter, pageable);

        return ResponseEntity.ok(items);
    }

    @Operation(
            summary = "Get clothing item by ID",
            description = "Retrieves a specific clothing item from the user's digital closet by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clothing item retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - item belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Clothing item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClothingItemDTO> getClothingItemById(
            @Parameter(description = "Clothing item ID")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.debug("Getting clothing item {} for user {}", id, userId);

        ClothingItemDTO item = clothingItemService.getById(userId, id);
        return ResponseEntity.ok(item);
    }

    @Operation(
            summary = "Update clothing item",
            description = "Updates an existing clothing item's attributes. The ID cannot be changed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clothing item updated successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - item belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Clothing item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClothingItemDTO> updateClothingItem(
            @Parameter(description = "Clothing item ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateClothingItemRequest request,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.info("Updating clothing item {} for user {}", id, userId);

        ClothingItemDTO updated = clothingItemService.update(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete clothing item",
            description = "Deletes a clothing item from the user's digital closet. If the item is referenced in any outfits, those outfits will be marked as incomplete."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Clothing item deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - item belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Clothing item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClothingItem(
            @Parameter(description = "Clothing item ID")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.info("Deleting clothing item {} for user {}", id, userId);

        clothingItemService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Upload or replace photo",
            description = "Uploads a new photo for an existing clothing item or replaces the current photo. " +
                    "Supports JPEG, PNG, and GIF formats up to 5MB. Old photo is automatically deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Photo uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - item belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Clothing item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "File size exceeds 5MB limit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingItemDTO> uploadPhoto(
            @Parameter(description = "Clothing item ID")
            @PathVariable Long id,
            @Parameter(description = "Photo file (JPEG, PNG, or GIF, max 5MB)")
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        log.info("Uploading photo for clothing item {} for user {}", id, userId);

        ClothingItemDTO updated = clothingItemService.uploadPhoto(userId, id, photo);
        return ResponseEntity.ok(updated);
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
