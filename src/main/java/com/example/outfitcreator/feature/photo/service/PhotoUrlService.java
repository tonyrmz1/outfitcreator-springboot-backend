package com.example.outfitcreator.feature.photo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class PhotoUrlService {

    @Value("${app.base-url}")
    private String baseUrl;

    public String generatePhotoUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }

        // For local storage
        String filename = Paths.get(photoPath).getFileName().toString();
        return String.format("%s/api/photos/%s", baseUrl, filename);
    }

    public String generateThumbnailUrl(String photoPath) {
        if (photoPath == null) {
            return null;
        }

        String filename = Paths.get(photoPath).getFileName().toString();
        String thumbFilename = filename.replace(".", "_thumb.");
        return String.format("%s/api/photos/%s", baseUrl, thumbFilename);
    }
}
