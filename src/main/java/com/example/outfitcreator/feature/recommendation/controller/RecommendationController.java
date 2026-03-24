package com.example.outfitcreator.feature.recommendation.controller;

import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.shared.exception.ErrorResponse;
import com.example.outfitcreator.feature.recommendation.dto.response.OutfitRecommendation;
import com.example.outfitcreator.feature.recommendation.dto.request.RecommendationRequest;
import com.example.outfitcreator.feature.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for outfit recommendations.
 */
@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Intelligent outfit recommendations based on color theory, fit compatibility, and seasonal appropriateness")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * @param recommendationService generates ranked outfit suggestions from the user's closet
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Get outfit recommendations based on query parameters.
     *
     * @param season the season filter (optional)
     * @param occasion the occasion filter (optional)
     * @param colorPreference the preferred color palette (optional)
     * @param limit the number of recommendations (default 10, max 20)
     * @param authentication the authenticated user
     * @return list of outfit recommendations
     */
    @Operation(
            summary = "Get outfit recommendations",
            description = "Generates intelligent outfit recommendations from the user's digital closet. " +
                    "The recommendation engine analyzes color compatibility using color theory, " +
                    "ensures fit balance (avoiding tight-tight or loose-loose combinations), " +
                    "considers seasonal appropriateness, and prioritizes less-worn items. " +
                    "Returns an empty list if no suitable recommendations are found."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recommendations generated successfully (may be empty list)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OutfitRecommendation.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<OutfitRecommendation>> getRecommendations(
            @Parameter(description = "Filter by season (SPRING, SUMMER, AUTUMN, WINTER, ALL_SEASON)")
            @RequestParam(required = false) Season season,
            @Parameter(description = "Filter by occasion (e.g., casual, formal, business)")
            @RequestParam(required = false) String occasion,
            @Parameter(description = "Preferred color palette for recommendations")
            @RequestParam(required = false) String colorPreference,
            @Parameter(description = "Number of recommendations to return (default 10, max 20)")
            @RequestParam(defaultValue = "10") Integer limit,
            Authentication authentication) {
        
        // Extract userId from SecurityContext
        Long userId = (Long) authentication.getPrincipal();
        
        // Validate limit parameter (max 20)
        if (limit > 20) {
            limit = 20;
        }
        
        // Build recommendation request
        RecommendationRequest request = RecommendationRequest.builder()
                .season(season)
                .occasion(occasion)
                .colorPreference(colorPreference)
                .limit(limit)
                .build();
        
        // Generate recommendations
        List<OutfitRecommendation> recommendations = 
                recommendationService.generateRecommendations(userId, request);
        
        // Return empty list with 200 OK when no recommendations found
        return ResponseEntity.ok(recommendations);
    }
}
