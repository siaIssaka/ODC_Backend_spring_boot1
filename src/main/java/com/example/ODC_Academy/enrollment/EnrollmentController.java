package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Inscriptions", description = "Inscriptions des apprenants aux formations")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SecurityUtils securityUtils;

    public EnrollmentController(EnrollmentService enrollmentService, SecurityUtils securityUtils) {
        this.enrollmentService = enrollmentService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Inscriptions d'un utilisateur (soi-même ou ADMIN)")
    public List<EnrollmentDTO> getEnrollmentsByUser(@PathVariable Long userId) {
        securityUtils.assertSelfOrAdmin(userId);
        return enrollmentService.getEnrollmentsByUser(userId).stream()
                .map(EnrollmentMapper::toDto)
                .toList();
    }

    @PostMapping("/user/{userId}/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscrire un apprenant à un cours (ADMIN uniquement)")
    public ResponseEntity<EnrollmentDTO> enrollUserToCourse(@PathVariable Long userId,
                                                            @PathVariable Long courseId) {
        securityUtils.assertSelfOrAdmin(userId);
        Enrollment enrollment = enrollmentService.enrollUserToCourse(userId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentMapper.toDto(enrollment));
    }

    @GetMapping("/course/{courseId}/candidates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Liste des apprenants actifs non inscrits au cours (ADMIN uniquement)")
    public EnrollmentCandidatesDTO getCourseCandidates(@PathVariable Long courseId) {
        return enrollmentService.getCourseCandidates(courseId);
    }

    @PostMapping("/course/{courseId}/learners")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscrire plusieurs apprenants actifs à un cours (ADMIN uniquement)")
    public EnrollmentBatchResult enrollUsersToCourse(@PathVariable Long courseId,
                                                     @Valid @RequestBody EnrollmentBatchRequest request) {
        return enrollmentService.enrollUsersToCourse(courseId, request.userIds());
    }

}
