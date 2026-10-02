package com.example.ODC_Academy.media;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaStorageService storage;

    public MediaController(MediaStorageService storage) { this.storage = storage; }

    @GetMapping("/{key:.+}")
    public ResponseEntity<Resource> get(@PathVariable String key,
                                        @RequestParam(defaultValue = "false") boolean download) {
        boolean video = key.endsWith(".mp4") || key.endsWith(".webm") || key.endsWith(".mov");
        boolean pdf = key.endsWith(".pdf");
        boolean inline = !download && (video || pdf || key.endsWith(".png") || key.endsWith(".jpg")
                || key.endsWith(".jpeg") || key.endsWith(".webp"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.contentType(key)))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(inline ? "inline" : "attachment")
                        .filename(key, StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(storage.load(key));
    }
}
