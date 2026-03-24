package com.example.outfitcreator.feature.photo.controller;

import com.example.outfitcreator.shared.exception.ErrorResponse;
import com.example.outfitcreator.feature.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves stored clothing photos by filename with path-traversal checks; upload is handled via closet endpoints.
 */
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photos", description = "Photo retrieval endpoints for clothing item images")
public class PhotoController {

    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    @Autowired
    private PhotoService photoService;

    @Value("${storage.base-path}")
    private String basePath;

    @Operation(
            summary = "Get photo by filename",
            description = "Retrieves a clothing item photo by its filename. Photos are served inline for display in browsers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Photo retrieved successfully",
                    content = @Content(mediaType = "image/jpeg")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Photo not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving photo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getPhoto(
            @Parameter(description = "Photo filename (e.g., item_123_1234567890.jpg)")
            @PathVariable String filename) {
        try {
            // Reject filenames containing path separators or traversal sequences
            if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }

            Path photosDir = Paths.get(basePath, "photos").toAbsolutePath().normalize();
            Path filePath = photosDir.resolve(filename).normalize();

            // Ensure the resolved path stays within the photos directory
            if (!filePath.startsWith(photosDir)) {
                log.warn("Path traversal attempt detected for filename: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            log.info("Attempting to load photo from: {}", filePath);

            // Check if file exists
            if (!Files.exists(filePath)) {
                log.warn("Photo not found at: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // Load file as Resource
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Photo not readable: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("Successfully serving photo: {}", filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .body(resource);

        } catch (IOException e) {
            log.error("Error retrieving photo: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
