package com.example.ODC_Academy.category;

import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.media.MediaStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Catégories du catalogue : lecture publique ; création, édition, photo et désactivation réservées à l'ADMIN. */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    public record CategoryRequest(@NotBlank String name, String description) { }

    private final CategoryRepository repo;
    private final MediaStorageService storage;

    public CategoryController(CategoryRepository repo, MediaStorageService storage) { this.repo = repo; this.storage = storage; }

    private Category find(Long id) { return repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Catégorie", id)); }

    @GetMapping
    public List<Category> all() { return repo.findAll().stream().filter(Category::isActive).toList(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody CategoryRequest r) {
        return repo.save(Category.builder().name(r.name().trim()).description(r.description()).build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
        Category c = find(id);
        c.setName(r.name().trim());
        c.setDescription(r.description());
        return repo.save(c);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Category image(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Category c = find(id);
        String key = storage.storeImage(file);
        if (c.getImageKey() != null) storage.delete(c.getImageKey());
        c.setImageKey(key);
        return repo.save(c);
    }

    /** Désactivation (masquée du menu) plutôt que suppression : les formations rattachées restent intactes. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        Category c = find(id);
        c.setActive(false);
        repo.save(c);
    }
}
