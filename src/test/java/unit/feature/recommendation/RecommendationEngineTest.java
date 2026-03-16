package unit.feature.recommendation;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.feature.recommendation.service.RecommendationEngine;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineTest {

    @Mock
    private ClothingItemRepository clothingItemRepository;

    private RecommendationEngine recommendationEngine;

    @BeforeEach
    void setUp() {
        recommendationEngine = new RecommendationEngine(clothingItemRepository);
    }

    @Test
    void calculateColorCompatibility_shouldReturn95ForNeutralColors() {
        ClothingItem whiteShirt = createItem("white");
        ClothingItem blackPants = createItem("black");

        double score = recommendationEngine.calculateColorCompatibility(whiteShirt, blackPants);

        assertThat(score).isEqualTo(95.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn95WhenOneColorIsNeutral() {
        ClothingItem whiteShirt = createItem("white");
        ClothingItem bluePants = createItem("blue");

        double score = recommendationEngine.calculateColorCompatibility(whiteShirt, bluePants);

        assertThat(score).isEqualTo(95.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn90ForMonochromaticColors() {
        ClothingItem redShirt = createItem("red");
        ClothingItem redPants = createItem("red");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, redPants);

        assertThat(score).isEqualTo(90.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn90ForAnalogousColors() {
        ClothingItem redShirt = createItem("red");
        ClothingItem orangePants = createItem("orange");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, orangePants);

        assertThat(score).isEqualTo(90.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn85ForComplementaryColors() {
        ClothingItem redShirt = createItem("red");
        ClothingItem cyanPants = createItem("cyan");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, cyanPants);

        assertThat(score).isEqualTo(85.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn85ForComplementaryColorsWithinRange() {
        ClothingItem yellowShirt = createItem("yellow");
        ClothingItem bluePants = createItem("blue");

        double score = recommendationEngine.calculateColorCompatibility(yellowShirt, bluePants);

        assertThat(score).isEqualTo(85.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn80ForTriadicColors() {
        ClothingItem redShirt = createItem("red");
        ClothingItem greenPants = createItem("green");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, greenPants);

        assertThat(score).isEqualTo(80.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn75ForAnalogousExtended() {
        ClothingItem redShirt = createItem("red");
        ClothingItem yellowPants = createItem("yellow");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, yellowPants);

        assertThat(score).isEqualTo(75.0);
    }

    @Test
    void calculateColorCompatibility_shouldReturn50ForOtherCombinations() {
        ClothingItem redShirt = createItem("red");
        ClothingItem limePants = createItem("lime");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, limePants);

        assertThat(score).isEqualTo(50.0);
    }

    @Test
    void calculateColorCompatibility_shouldHandleWrapAroundCorrectly() {
        ClothingItem redShirt = createItem("red");
        ClothingItem pinkPants = createItem("pink");

        double score = recommendationEngine.calculateColorCompatibility(redShirt, pinkPants);

        assertThat(score).isEqualTo(90.0);
    }

    @Test
    void calculateColorCompatibility_shouldHandleWrapAroundForComplementary() {
        ClothingItem magentaShirt = createItem("magenta");
        ClothingItem greenPants = createItem("green");

        double score = recommendationEngine.calculateColorCompatibility(magentaShirt, greenPants);

        assertThat(score).isEqualTo(85.0);
    }

    @Test
    void calculateColorCompatibility_shouldUsePrimaryColorOnly() {
        ClothingItem item1 = createItemWithSecondaryColor("red", "blue");
        ClothingItem item2 = createItemWithSecondaryColor("orange", "green");

        double score = recommendationEngine.calculateColorCompatibility(item1, item2);

        assertThat(score).isEqualTo(90.0);
    }

    @Test
    void calculateColorCompatibility_shouldHandleGrayAsNeutral() {
        ClothingItem grayShirt = createItem("gray");
        ClothingItem redPants = createItem("red");

        double score = recommendationEngine.calculateColorCompatibility(grayShirt, redPants);

        assertThat(score).isEqualTo(95.0);
    }

    @Test
    void calculateColorCompatibility_shouldHandleBeigeAsNeutral() {
        ClothingItem beigeShirt = createItem("beige");
        ClothingItem bluePants = createItem("blue");

        double score = recommendationEngine.calculateColorCompatibility(beigeShirt, bluePants);

        assertThat(score).isEqualTo(95.0);
    }

    private ClothingItem createItem(String primaryColor) {
        ClothingItem item = new ClothingItem();
        item.setPrimaryColor(primaryColor);
        item.setCategory(ClothingCategory.TOP);
        return item;
    }

    private ClothingItem createItemWithSecondaryColor(String primaryColor, String secondaryColor) {
        ClothingItem item = new ClothingItem();
        item.setPrimaryColor(primaryColor);
        item.setSecondaryColor(secondaryColor);
        item.setCategory(ClothingCategory.TOP);
        return item;
    }
}
