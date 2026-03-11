package com.example.outfitcreator.photo;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    @Value("${storage.base-path}")
    private String basePath;

    @Value("${storage.max-file-size}")
    private long maxFileSize;

    @Value("${storage.max-resolution}")
    private int maxResolution;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private static final int THUMBNAIL_SIZE = 300;

    public String uploadPhoto(MultipartFile file, Long itemId) throws IOException {
        // 1. Validate file type
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException("Only JPEG, PNG, and GIF are supported");
        }

        // 2. Validate file size
        if (file.getSize() > maxFileSize) {
            throw new FileSizeExceededException("File size exceeds 5MB limit");
        }

        // 3. Generate unique filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getExtension(file.getOriginalFilename());
        String filename = String.format("item_%d_%s.%s", itemId, timestamp, extension);

        // 4. Read and process image
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new InvalidFileTypeException("Unable to read image file");
        }

        // 5. Resize if necessary
        if (image.getWidth() > maxResolution || image.getHeight() > maxResolution) {
            image = resizeImage(image, maxResolution, maxResolution);
        }

        // 6. Save to storage
        Path photoPath = Paths.get(basePath, "photos", filename);
        Files.createDirectories(photoPath.getParent());
        ImageIO.write(image, extension, photoPath.toFile());

        // 7. Generate thumbnail
        BufferedImage thumbnail = resizeImage(image, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        Path thumbPath = Paths.get(basePath, "photos",
                String.format("item_%d_%s_thumb.%s", itemId, timestamp, extension));
        ImageIO.write(thumbnail, extension, thumbPath.toFile());

        return photoPath.toString();
    }

    public void deletePhoto(String photoPath) {
        try {
            Files.deleteIfExists(Paths.get(photoPath));

            // Delete thumbnail
            String thumbPath = photoPath.replace(".", "_thumb.");
            Files.deleteIfExists(Paths.get(thumbPath));
        } catch (IOException e) {
            log.error("Failed to delete photo: {}", photoPath, e);
        }
    }

    public byte[] getPhoto(String photoPath) throws IOException {
        return Files.readAllBytes(Paths.get(photoPath));
    }

    public BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate scaling factor
        double scale = Math.min(
                (double) maxWidth / width,
                (double) maxHeight / height
        );

        if (scale >= 1.0) {
            return original; // No resize needed
        }

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        // Use Thumbnailator for high-quality resizing
        return Thumbnails.of(original)
                .size(newWidth, newHeight)
                .asBufferedImage();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
