package com.example.ODC_Academy.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void localImageStorageStillWorksAndCleanupIsIdempotent() throws Exception {
        MediaStorageService storage = new MediaStorageService(
                tempDir.toString(), "local", "", "", "");
        MockMultipartFile image = new MockMultipartFile(
                "file", "course.png", "image/png", new byte[]{1, 2, 3});

        String key = storage.storeImage(image);

        assertTrue(Files.isRegularFile(tempDir.resolve(key)));
        Files.delete(tempDir.resolve(key));
        Files.delete(tempDir);
        assertDoesNotThrow(() -> storage.delete(key));
    }

    @Test
    void cloudinaryProviderRequiresAllCredentials() {
        assertThrows(IllegalStateException.class, () -> new MediaStorageService(
                tempDir.toString(), "cloudinary", "cloud-name", "", ""));
    }
}
