package com.example.outfitcreator.feature.outfit.controller;

import com.example.outfitcreator.shared.exception.ErrorResponse;
import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import com.example.outfitcreator.feature.outfit.service.OutfitService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for creating, updating, listing, and deleting user outfits built from closet items.
 */
@RestController
@RequestMapping("/api/outfits")
@Tag(name = "Outfits", description = "Outfit creation and management - combine clothing items into complete outfits")
@SecurityRequirement(name = "bearerAuth")
public class OutfitController {

    private final OutfitService outfitService;

    /**
     * @param outfitService outfit domain service
     */
    public OutfitController(OutfitService outfitService) {
        this.outfitService = outfitService;
    }

    @Operation(
            summary = "Create a new outfit",
            description = "Creates a new outfit by combining multiple clothing items from the user's digital closet. " +
                    "Each item is assigned a position (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Outfit created successfully",
                    content = @Content(schema = @Schema(implementation = OutfitDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or clothing item references",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<OutfitDTO> createOutfit(
            @Valid @RequestBody CreateOutfitRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OutfitDTO outfit = outfitService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(outfit);
    }

    @Operation(
            summary = "Get all outfits",
            description = "Retrieves all outfits created by the authenticated user with pagination support."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Outfits retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<OutfitDTO>> getAllOutfits(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<OutfitDTO> outfits = outfitService.findAll(userId, pageable);
        return ResponseEntity.ok(outfits);
    }

    @Operation(
            summary = "Get outfit by ID",
            description = "Retrieves a specific outfit by its ID, including all clothing items and their positions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Outfit retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OutfitDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - outfit belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Outfit not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OutfitDTO> getOutfit(
            @Parameter(description = "Outfit ID")
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OutfitDTO outfit = outfitService.getById(userId, id);
        return ResponseEntity.ok(outfit);
    }

    @Operation(
            summary = "Update outfit",
            description = "Updates an outfit's name and notes. Clothing items cannot be modified through this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Outfit updated successfully",
                    content = @Content(schema = @Schema(implementation = OutfitDTO.class))
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
                    description = "Forbidden - outfit belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Outfit not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<OutfitDTO> updateOutfit(
            @Parameter(description = "Outfit ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateOutfitRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OutfitDTO outfit = outfitService.update(userId, id, request);
        return ResponseEntity.ok(outfit);
    }

    @Operation(
            summary = "Delete outfit",
            description = "Deletes an outfit from the user's collection. Clothing items are not affected."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Outfit deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - outfit belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Outfit not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutfit(
            @Parameter(description = "Outfit ID")
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        outfitService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
