package com.example.outfitcreator.feature.recommendation.service;

import com.example.outfitcreator.feature.recommendation.dto.request.RecommendationRequest;
import com.example.outfitcreator.feature.recommendation.dto.response.OutfitRecommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of RecommendationService that delegates to RecommendationEngine.
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationEngine recommendationEngine;

    @Override
    public List<OutfitRecommendation> generateRecommendations(Long userId, RecommendationRequest request) {
        return recommendationEngine.generateRecommendations(userId, request);
    }
}
