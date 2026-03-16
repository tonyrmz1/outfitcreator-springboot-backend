package com.example.outfitcreator.feature.recommendation.service;

import com.example.outfitcreator.feature.recommendation.dto.request.RecommendationRequest;
import com.example.outfitcreator.feature.recommendation.dto.response.OutfitRecommendation;

import java.util.List;

/**
 * Service interface for generating outfit recommendations.
 */
public interface RecommendationService {

    /**
     * Generate outfit recommendations for a user based on their digital closet.
     *
     * @param userId  the user ID
     * @param request the recommendation request with filters and limit
     * @return list of outfit recommendations sorted by overall score
     */
    List<OutfitRecommendation> generateRecommendations(Long userId, RecommendationRequest request);
}
