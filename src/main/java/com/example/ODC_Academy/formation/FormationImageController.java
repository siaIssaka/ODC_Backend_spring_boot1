package com.example.ODC_Academy.formation;

import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.media.MediaStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/formations")
public class FormationImageController {
    private final FormationRepository formations;
    private final MediaStorageService storage;

    public FormationImageController(FormationRepository f, MediaStorageService s) { this.formations = f; this.storage = s; }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public FormationDTO upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Formation f = formations.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Formation", id));
        String key = storage.storeImage(file);
        if (f.getImageKey() != null) storage.delete(f.getImageKey());
        f.setImageKey(key);
        return FormationMapper.toDto(formations.save(f));
    }
}
