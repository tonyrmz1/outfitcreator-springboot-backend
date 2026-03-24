package com.example.outfitcreator.feature.photo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Stores, deletes, and reads clothing item image files on the configured filesystem.
 */
public interface PhotoService {

    /**
     * Validates, optionally resizes, saves the image and a thumbnail; returns the stored path string.
     */
    String uploadPhoto(MultipartFile file, Long itemId) throws IOException;

    void deletePhoto(String photoPath);

    byte[] getPhoto(String photoPath) throws IOException;
}
