package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Progression", description = "Suivi de la progression des apprenants dans leurs formations")
@SecurityRequirement(name = "bearerAuth")
public class ProgressController {

    private final ProgressService progressService;
    private final SecurityUtils securityUtils;

    public ProgressController(ProgressService progressService, SecurityUtils securityUtils) {
        this.progressService = progressService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Progressions d'un utilisateur (soi-même ou ADMIN)")
    public List<ProgressDTO> getProgressByUser(@PathVariable Long userId) {
        securityUtils.assertSelfOrAdmin(userId);
        return progressService.getProgressByUser(userId).stream()
                .map(ProgressMapper::toDto)
                .toList();
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    @Operation(summary = "Progression d'un utilisateur sur un cours (soi-même ou ADMIN)")
    public ProgressDTO getProgress(@PathVariable Long userId, @PathVariable Long courseId) {
        securityUtils.assertSelfOrAdmin(userId);
        return progressService.getProgress(userId, courseId)
                .map(ProgressMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune progression trouvée pour cet utilisateur et ce cours"));
    }

    @PostMapping("/user/{userId}/course/{courseId}")
    @Operation(summary = "Créer une progression (soi-même ou ADMIN)")
    public ResponseEntity<ProgressDTO> createProgress(@PathVariable Long userId,
                                                      @PathVariable Long courseId) {
        securityUtils.assertSelfOrAdmin(userId);
        Progress progress = progressService.createProgress(userId, courseId);
        return ResponseEntity.ok(ProgressMapper.toDto(progress));
    }

    @PutMapping("/user/{userId}/course/{courseId}")
    @Operation(summary = "Mettre à jour une progression (soi-même ou ADMIN)")
    public ProgressDTO updateProgress(@PathVariable Long userId,
                                      @PathVariable Long courseId,
                                      @Valid @RequestBody ProgressUpdateRequestDTO request) {
        securityUtils.assertSelfOrAdmin(userId);
        Progress updated = progressService.updateProgress(
                userId, courseId, request.completedLessons(), request.totalLessons());
        return ProgressMapper.toDto(updated);
    }
}
