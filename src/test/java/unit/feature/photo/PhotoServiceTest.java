package unit.feature.photo;

import com.example.outfitcreator.feature.photo.exception.FileSizeExceededException;
import com.example.outfitcreator.feature.photo.exception.InvalidFileTypeException;
import com.example.outfitcreator.feature.photo.service.PhotoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhotoServiceTest {

    private PhotoServiceImpl photoService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        photoService = new PhotoServiceImpl();
        ReflectionTestUtils.setField(photoService, "basePath", tempDir.toString());
        ReflectionTestUtils.setField(photoService, "maxFileSize", 5242880L);
        ReflectionTestUtils.setField(photoService, "maxResolution", 1920);
    }

    @Test
    void uploadPhoto_withValidJpeg_shouldSucceed() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        MockMultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", baos.toByteArray());

        String photoPath = photoService.uploadPhoto(file, 1L);

        assertThat(photoPath).isNotNull();
        assertThat(Files.exists(Paths.get(photoPath))).isTrue();

        String thumbPath = photoPath.replace(".", "_thumb.");
        assertThat(Files.exists(Paths.get(thumbPath))).isTrue();
    }

    @Test
    void uploadPhoto_withInvalidFileType_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "test.txt", "text/plain", "test content".getBytes());

        assertThatThrownBy(() -> photoService.uploadPhoto(file, 1L))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessageContaining("Only JPEG, PNG, and GIF are supported");
    }

    @Test
    void uploadPhoto_withOversizedFile_shouldThrowException() {
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "photo", "large.jpg", "image/jpeg", largeContent);

        assertThatThrownBy(() -> photoService.uploadPhoto(file, 1L))
                .isInstanceOf(FileSizeExceededException.class)
                .hasMessageContaining("File size exceeds 5MB limit");
    }

    @Test
    void uploadPhoto_withLargeImage_shouldResize() throws IOException {
        BufferedImage largeImage = new BufferedImage(2500, 2500, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(largeImage, "jpg", baos);

        MockMultipartFile file = new MockMultipartFile(
                "photo", "large.jpg", "image/jpeg", baos.toByteArray());

        String photoPath = photoService.uploadPhoto(file, 1L);

        BufferedImage savedImage = ImageIO.read(Paths.get(photoPath).toFile());

        assertThat(savedImage.getWidth()).isLessThanOrEqualTo(1920);
        assertThat(savedImage.getHeight()).isLessThanOrEqualTo(1920);
    }

    @Test
    void deletePhoto_shouldRemovePhotoAndThumbnail() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        MockMultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", baos.toByteArray());

        String photoPath = photoService.uploadPhoto(file, 1L);
        String thumbPath = photoPath.replace(".", "_thumb.");

        assertThat(Files.exists(Paths.get(photoPath))).isTrue();
        assertThat(Files.exists(Paths.get(thumbPath))).isTrue();

        photoService.deletePhoto(photoPath);

        assertThat(Files.exists(Paths.get(photoPath))).isFalse();
        assertThat(Files.exists(Paths.get(thumbPath))).isFalse();
    }

    @Test
    void resizeImage_withSmallImage_shouldNotResize() throws IOException {
        BufferedImage smallImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = photoService.resizeImage(smallImage, 1920, 1920);

        assertThat(result).isSameAs(smallImage);
    }

    @Test
    void resizeImage_withLargeImage_shouldMaintainAspectRatio() throws IOException {
        BufferedImage largeImage = new BufferedImage(3000, 2000, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = photoService.resizeImage(largeImage, 1920, 1920);

        assertThat(result.getWidth()).isEqualTo(1920);
        assertThat(result.getHeight()).isEqualTo(1280);
    }
}
