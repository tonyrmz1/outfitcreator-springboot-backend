package com.example.outfitcreator.feature.photo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PhotoService {

    String uploadPhoto(MultipartFile file, Long itemId) throws IOException;

    void deletePhoto(String photoPath);

    byte[] getPhoto(String photoPath) throws IOException;
}
