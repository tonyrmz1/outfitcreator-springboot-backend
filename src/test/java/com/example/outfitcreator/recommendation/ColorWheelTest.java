package com.example.outfitcreator.recommendation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorWheelTest {
    
    @Test
    void getHue_shouldReturnCorrectHueForRed() {
        assertThat(ColorWheel.getHue("red")).isEqualTo(0);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForOrange() {
        assertThat(ColorWheel.getHue("orange")).isEqualTo(30);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForYellow() {
        assertThat(ColorWheel.getHue("yellow")).isEqualTo(60);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForLime() {
        assertThat(ColorWheel.getHue("lime")).isEqualTo(90);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForGreen() {
        assertThat(ColorWheel.getHue("green")).isEqualTo(120);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForCyan() {
        assertThat(ColorWheel.getHue("cyan")).isEqualTo(180);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForBlue() {
        assertThat(ColorWheel.getHue("blue")).isEqualTo(240);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForPurple() {
        assertThat(ColorWheel.getHue("purple")).isEqualTo(270);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForMagenta() {
        assertThat(ColorWheel.getHue("magenta")).isEqualTo(300);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForPink() {
        assertThat(ColorWheel.getHue("pink")).isEqualTo(330);
    }
    
    @Test
    void getHue_shouldReturnCorrectHueForBrown() {
        assertThat(ColorWheel.getHue("brown")).isEqualTo(25);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForWhite() {
        assertThat(ColorWheel.getHue("white")).isEqualTo(-1);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForBlack() {
        assertThat(ColorWheel.getHue("black")).isEqualTo(-1);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForGray() {
        assertThat(ColorWheel.getHue("gray")).isEqualTo(-1);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForBeige() {
        assertThat(ColorWheel.getHue("beige")).isEqualTo(-1);
    }
    
    @Test
    void getHue_shouldBeCaseInsensitive() {
        assertThat(ColorWheel.getHue("RED")).isEqualTo(0);
        assertThat(ColorWheel.getHue("Blue")).isEqualTo(240);
        assertThat(ColorWheel.getHue("GREEN")).isEqualTo(120);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForUnknownColor() {
        assertThat(ColorWheel.getHue("unknown")).isEqualTo(-1);
    }
    
    @Test
    void getHue_shouldReturnNegativeOneForNull() {
        assertThat(ColorWheel.getHue(null)).isEqualTo(-1);
    }
    
    @Test
    void isNeutral_shouldReturnTrueForWhite() {
        assertThat(ColorWheel.isNeutral("white")).isTrue();
    }
    
    @Test
    void isNeutral_shouldReturnTrueForBlack() {
        assertThat(ColorWheel.isNeutral("black")).isTrue();
    }
    
    @Test
    void isNeutral_shouldReturnTrueForGray() {
        assertThat(ColorWheel.isNeutral("gray")).isTrue();
    }
    
    @Test
    void isNeutral_shouldReturnTrueForBeige() {
        assertThat(ColorWheel.isNeutral("beige")).isTrue();
    }
    
    @Test
    void isNeutral_shouldReturnFalseForRed() {
        assertThat(ColorWheel.isNeutral("red")).isFalse();
    }
    
    @Test
    void isNeutral_shouldReturnFalseForBlue() {
        assertThat(ColorWheel.isNeutral("blue")).isFalse();
    }
    
    @Test
    void isNeutral_shouldReturnFalseForGreen() {
        assertThat(ColorWheel.isNeutral("green")).isFalse();
    }
    
    @Test
    void isNeutral_shouldReturnTrueForUnknownColor() {
        assertThat(ColorWheel.isNeutral("unknown")).isTrue();
    }
    
    @Test
    void isNeutral_shouldReturnTrueForNull() {
        assertThat(ColorWheel.isNeutral(null)).isTrue();
    }
}
