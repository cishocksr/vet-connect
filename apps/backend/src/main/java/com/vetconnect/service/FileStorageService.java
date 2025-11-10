package com.vetconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file storage
 *
 * FEATURES:
 * - Upload profile pictures
 * - Delete old profile pictures
 * - Generate unique filenames
 * - Validate file types and sizes
 *
 * STORAGE OPTIONS:
 * - Local storage (current implementation)
 * - AWS S3 (recommended for production)
 * - Cloudinary (easy alternative)
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private final Tika tika = new Tika();

    /**
     * Initialize upload directory on startup
     */
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Store profile picture
     *
     * @param file Uploaded file
     * @param userId User's UUID
     * @return Relative URL path to the stored file
     */
    public String storeProfilePicture(MultipartFile file, UUID userId) {
        // Validate file
        validateFile(file);

        try {
            // Ensure upload directory exists
            init();

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = userId + "-" + System.currentTimeMillis() + extension;

            // Store file
            Path targetLocation = Paths.get(uploadDir).resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored profile picture: {}", newFilename);

            // Return relative URL (frontend will prepend base URL)
            return "/uploads/profile-pictures/" + newFilename;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    /**
     * Delete profile picture
     *
     * @param fileUrl URL of file to delete
     */
    public void deleteProfilePicture(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted profile picture: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            // Don't throw exception - file deletion failure shouldn't break user flow
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of " +
                    (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        boolean validExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new RuntimeException("Invalid file type. Allowed types: jpg, jpeg, png, gif, webp");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }

        // NEW: Validate actual file content using magic bytes
        try {
            String detectedType = tika.detect(file.getInputStream());
            log.debug("Detected file type: {}", detectedType);

            if (!detectedType.startsWith("image/")) {
                log.warn("File content mismatch. Extension: {}, Declared type: {}, Detected type: {}",
                        extension, contentType, detectedType);
                throw new RuntimeException(
                        "File content does not match image type. Detected: " + detectedType
                );
            }
        } catch (IOException e) {
            log.error("Failed to detect file type", e);
            throw new RuntimeException("Failed to validate file content");
        }
    }
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot);
    }
}