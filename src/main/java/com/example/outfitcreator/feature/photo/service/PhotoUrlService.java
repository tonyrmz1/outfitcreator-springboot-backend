package com.example.outfitcreator.feature.photo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

/**
 * Builds absolute HTTP URLs for full-size and thumbnail images exposed by {@code /api/photos/{filename}}.
 */
@Service
public class PhotoUrlService {

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * @param photoPath filesystem path or filename segment as stored on the entity
     * @return public URL for the main image, or {@code null} if {@code photoPath} is null
     */
    public String generatePhotoUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }

        // For local storage
        String filename = Paths.get(photoPath).getFileName().toString();
        return String.format("%s/api/photos/%s", baseUrl, filename);
    }

    /**
     * @param photoPath filesystem path for the main image; thumbnail name is derived by inserting {@code _thumb} before the extension
     * @return public URL for the thumbnail, or {@code null} if {@code photoPath} is null
     */
    public String generateThumbnailUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }

        String filename = Paths.get(photoPath).getFileName().toString();
        String thumbFilename = filename.replace(".", "_thumb.");
        return String.format("%s/api/photos/%s", baseUrl, thumbFilename);
    }
}
