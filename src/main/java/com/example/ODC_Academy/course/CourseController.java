package com.example.ODC_Academy.course;

import com.example.ODC_Academy.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Cours", description = "Gestion des cours")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;
    private final SecurityUtils securityUtils;

    public CourseController(CourseService courseService, SecurityUtils securityUtils) {
        this.courseService = courseService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public List<CourseDTO> getAll() {
        return courseService.getAll().stream().map(CourseMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public CourseDTO getById(@PathVariable Long id) {
        return CourseMapper.toDto(courseService.getById(id));
    }

    @PostMapping("/formation/{formationId}")
    @PreAuthorize("hasRole('FORMATEUR')")
    @Operation(summary = "Créer un cours dans une formation attribuée au formateur")
    public ResponseEntity<CourseDTO> create(@PathVariable Long formationId,
                                            @Valid @RequestBody CourseRequestDTO request) {
        Course created = courseService.create(formationId, securityUtils.getCurrentUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    @Operation(summary = "Modifier un cours créé par le formateur connecté")
    public CourseDTO update(@PathVariable Long id, @Valid @RequestBody CourseRequestDTO request) {
        return CourseMapper.toDto(courseService.update(id, securityUtils.getCurrentUser().getId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    @Operation(summary = "Supprimer un cours créé ou géré par le formateur connecté")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id, securityUtils.getCurrentUser().getId());
        return ResponseEntity.noContent().build();
    }
}
