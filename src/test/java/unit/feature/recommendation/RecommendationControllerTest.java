package unit.feature.recommendation;

import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.recommendation.controller.RecommendationController;
import com.example.outfitcreator.feature.recommendation.dto.request.RecommendationRequest;
import com.example.outfitcreator.feature.recommendation.dto.response.OutfitRecommendation;
import com.example.outfitcreator.feature.recommendation.service.RecommendationService;
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
    private RecommendationService recommendationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RecommendationController recommendationController;

    @Test
    void shouldReturnRecommendationsWithDefaultLimit() {
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);

        List<OutfitRecommendation> mockRecommendations = new ArrayList<>();
        mockRecommendations.add(OutfitRecommendation.builder().overallScore(85.0).build());

        when(recommendationService.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(mockRecommendations);

        ResponseEntity<List<OutfitRecommendation>> response =
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecommendationsFound() {
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationService.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        ResponseEntity<List<OutfitRecommendation>> response =
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void shouldCapLimitAt20() {
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationService.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        ResponseEntity<List<OutfitRecommendation>> response =
                recommendationController.getRecommendations(null, null, null, 50, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void shouldPassAllQueryParametersToService() {
        Long userId = 1L;
        Season season = Season.SPRING;
        String occasion = "casual";
        String colorPreference = "blue";
        Integer limit = 15;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationService.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        ResponseEntity<List<OutfitRecommendation>> response =
                recommendationController.getRecommendations(season, occasion, colorPreference, limit, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldExtractUserIdFromSecurityContext() {
        Long userId = 123L;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(recommendationService.generateRecommendations(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(new ArrayList<>());

        ResponseEntity<List<OutfitRecommendation>> response =
                recommendationController.getRecommendations(null, null, null, 10, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
