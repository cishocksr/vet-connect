package com.vetconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService Tests")
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", 5242880L); // 5MB
    }

    @Test
    @DisplayName("Should initialize upload directory successfully")
    void testInit_Success() {
        // Act
        assertDoesNotThrow(() -> fileStorageService.init());

        // Assert
        assertTrue(Files.exists(tempDir));
    }

    @Test
    @DisplayName("Should store profile picture successfully")
    void testStoreProfilePicture_Success() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] content = createValidImageBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                content
        );

        // Act
        String result = fileStorageService.storeProfilePicture(file, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/profile-pictures/"));
        assertTrue(result.contains(userId.toString()));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void testStoreProfilePicture_EmptyFile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fileStorageService.storeProfilePicture(emptyFile, userId)
        );

        assertTrue(exception.getMessage().contains("empty file"));
    }

    @Test
    @DisplayName("Should throw exception when file size exceeds maximum")
    void testStoreProfilePicture_FileTooLarge() {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fileStorageService.storeProfilePicture(largeFile, userId)
        );

        assertTrue(exception.getMessage().contains("exceeds maximum"));
    }

    @Test
    @DisplayName("Should throw exception for invalid file extension")
    void testStoreProfilePicture_InvalidExtension() {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] content = createValidImageBytes();
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.exe",
                "image/jpeg",
                content
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fileStorageService.storeProfilePicture(invalidFile, userId)
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    @DisplayName("Should throw exception for non-image content type")
    void testStoreProfilePicture_InvalidContentType() {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] content = createValidImageBytes();
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "text/plain",
                content
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fileStorageService.storeProfilePicture(invalidFile, userId)
        );

        assertTrue(exception.getMessage().contains("must be an image"));
    }

    @Test
    @DisplayName("Should delete profile picture successfully")
    void testDeleteProfilePicture_Success() throws IOException {
        // Arrange
        String filename = "test-file.jpg";
        Path testFile = tempDir.resolve(filename);
        Files.createFile(testFile);
        assertTrue(Files.exists(testFile));

        String fileUrl = "/uploads/profile-pictures/" + filename;

        // Act
        fileStorageService.deleteProfilePicture(fileUrl);

        // Assert
        assertFalse(Files.exists(testFile));
    }

    @Test
    @DisplayName("Should handle deleting non-existent file gracefully")
    void testDeleteProfilePicture_NonExistent() {
        // Arrange
        String fileUrl = "/uploads/profile-pictures/nonexistent.jpg";

        // Act & Assert
        assertDoesNotThrow(() -> fileStorageService.deleteProfilePicture(fileUrl));
    }

    @Test
    @DisplayName("Should handle null file URL gracefully")
    void testDeleteProfilePicture_NullUrl() {
        // Act & Assert
        assertDoesNotThrow(() -> fileStorageService.deleteProfilePicture(null));
    }

    @Test
    @DisplayName("Should handle empty file URL gracefully")
    void testDeleteProfilePicture_EmptyUrl() {
        // Act & Assert
        assertDoesNotThrow(() -> fileStorageService.deleteProfilePicture(""));
    }

    /**
     * Create a minimal valid JPEG byte array (JPEG magic bytes)
     */
    private byte[] createValidImageBytes() {
        // JPEG magic bytes: FF D8 FF E0
        byte[] jpegHeader = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };
        byte[] content = new byte[1024];
        System.arraycopy(jpegHeader, 0, content, 0, jpegHeader.length);
        return content;
    }
}
