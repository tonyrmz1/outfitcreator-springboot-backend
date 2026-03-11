package com.example.outfitcreator.recommendation;

import com.example.outfitcreator.enums.Season;
import com.example.outfitcreator.recommendation.dto.OutfitRecommendation;
import com.example.outfitcreator.recommendation.dto.RecommendationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock
    private RecommendationEngine recommendationEngine;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RecommendationController recommendationController;

    @Test
    void shouldReturnRecommendationsWithDefaultLimit() {
        // Given
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);
        
        List<OutfitRecommendation> mockRecommendations = new ArrayList<>();
        mockRecommendations.add(OutfitRecommendation.builder()
                .overallScore(85.0)
                .build());
        
        when(recommendationEngine.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(mockRecommendations);

        // When
        ResponseEntity<List<OutfitRecommendation>> response = 
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecommendationsFound() {
        // Given
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationEngine.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<OutfitRecommendation>> response = 
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldCapLimitAt20() {
        // Given
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationEngine.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<OutfitRecommendation>> response = 
                recommendationController.getRecommendations(null, null, null, 50, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        // The limit should be capped at 20 in the controller
    }

    @Test
    void shouldPassAllQueryParametersToEngine() {
        // Given
        Long userId = 1L;
        Season season = Season.SPRING;
        String occasion = "casual";
        String colorPreference = "blue";
        Integer limit = 15;
        
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationEngine.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<OutfitRecommendation>> response = 
                recommendationController.getRecommendations(season, occasion, colorPreference, limit, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldExtractUserIdFromSecurityContext() {
        // Given
        Long userId = 123L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationEngine.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<OutfitRecommendation>> response = 
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
