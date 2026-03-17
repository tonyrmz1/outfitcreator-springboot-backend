package com.example.outfitcreator.feature.closet.controller;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.closet.dto.request.CreateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.request.UpdateClothingItemRequest;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemDTO;
import com.example.outfitcreator.feature.closet.service.ClothingItemService;
import com.example.outfitcreator.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/clothing")
@Tag(name = "Closet", description = "Clothing item management - add, view, update and delete items in your digital closet")
@SecurityRequirement(name = "bearerAuth")
public class ClothingItemController {

    private final ClothingItemService clothingItemService;

    public ClothingItemController(ClothingItemService clothingItemService) {
        this.clothingItemService = clothingItemService;
    }

    @Operation(summary = "Get all clothing items", description = "Retrieves all clothing items for the authenticated user with pagination support.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ClothingItemDTO>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by category") @RequestParam(required = false) ClothingCategory category,
            @Parameter(description = "Filter by season") @RequestParam(required = false) Season season,
            @Parameter(description = "Filter by primary color") @RequestParam(required = false) String color,
            @Parameter(description = "Search by name or brand") @RequestParam(required = false) String searchQuery,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(clothingItemService.getAll(userId, pageable, category, season, color, searchQuery));
    }

    @Operation(summary = "Get clothing item by ID", description = "Retrieves a single clothing item by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClothingItemDTO> getById(
            @Parameter(description = "Clothing item ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(clothingItemService.getItem(userId, id));
    }

    @Operation(summary = "Create a new clothing item", description = "Creates a new clothing item, optionally with a photo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingItemDTO> create(
            @Valid @ModelAttribute CreateClothingItemRequest request,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) throws IOException {
        Long userId = (Long) authentication.getPrincipal();
        ClothingItemDTO created = clothingItemService.createItem(userId, request, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a clothing item", description = "Updates an existing clothing item's details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClothingItemDTO> update(
            @Parameter(description = "Clothing item ID") @PathVariable Long id,
            @Valid @RequestBody UpdateClothingItemRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(clothingItemService.updateItem(userId, id, request));
    }

    @Operation(summary = "Delete a clothing item", description = "Deletes a clothing item and its associated photo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Clothing item ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        clothingItemService.deleteItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload photo for a clothing item", description = "Uploads or replaces the photo for a clothing item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ClothingItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingItemDTO> uploadPhoto(
            @Parameter(description = "Clothing item ID") @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication) throws IOException {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(clothingItemService.uploadPhoto(userId, id, photo));
    }
}
