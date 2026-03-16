package unit.feature.photo;

import com.example.outfitcreator.feature.photo.service.PhotoUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoUrlServiceTest {

    private PhotoUrlService photoUrlService;

    @BeforeEach
    void setUp() {
        photoUrlService = new PhotoUrlService();
        ReflectionTestUtils.setField(photoUrlService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void generatePhotoUrl_withValidPath_shouldReturnUrl() {
        String photoPath = "storage/photos/item_1_1234567890.jpg";

        String url = photoUrlService.generatePhotoUrl(photoPath);

        assertThat(url).isEqualTo("http://localhost:8080/api/photos/item_1_1234567890.jpg");
    }

    @Test
    void generatePhotoUrl_withNullPath_shouldReturnNull() {
        String url = photoUrlService.generatePhotoUrl(null);

        assertThat(url).isNull();
    }

    @Test
    void generateThumbnailUrl_withValidPath_shouldReturnThumbnailUrl() {
        String photoPath = "storage/photos/item_1_1234567890.jpg";

        String url = photoUrlService.generateThumbnailUrl(photoPath);

        assertThat(url).isEqualTo("http://localhost:8080/api/photos/item_1_1234567890_thumb.jpg");
    }

    @Test
    void generateThumbnailUrl_withNullPath_shouldReturnNull() {
        String url = photoUrlService.generateThumbnailUrl(null);

        assertThat(url).isNull();
    }

    @Test
    void generateThumbnailUrl_withPngFile_shouldHandleCorrectly() {
        String photoPath = "storage/photos/item_2_9876543210.png";

        String url = photoUrlService.generateThumbnailUrl(photoPath);

        assertThat(url).isEqualTo("http://localhost:8080/api/photos/item_2_9876543210_thumb.png");
    }
}
