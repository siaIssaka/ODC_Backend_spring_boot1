package com.example.ODC_Academy.coursesession;

import com.example.ODC_Academy.enrollment.EnrollmentBatchRequest;
import com.example.ODC_Academy.enrollment.EnrollmentBatchResult;
import com.example.ODC_Academy.enrollment.EnrollmentCandidatesDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
public class CourseSessionController {
    private final CourseSessionService service;

    public CourseSessionController(CourseSessionService service) {
        this.service = service;
    }

    @GetMapping("/admin/cohorts")
    @Operation(summary = "Lister les cohortes, leurs cours et apprenants inscrits (ADMIN)")
    public List<AdminCohortOverviewDTO> getAdminOverview() {
        return service.getAdminOverview();
    }

    @GetMapping("/formations/{formationId}/sessions")
    @Operation(summary = "Lister les cohortes d'une formation (ADMIN)")
    public List<CourseSessionDTO> getFormationSessions(@PathVariable Long formationId) {
        return service.getFormationSessions(formationId);
    }

    @PostMapping("/formations/{formationId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une cohorte et lui rattacher plusieurs cours (ADMIN)")
    public CourseSessionDTO create(@PathVariable Long formationId,
                                   @Valid @RequestBody CreateCourseSessionRequest request) {
        return service.create(formationId, request);
    }

    @PostMapping("/course-sessions/{sessionId}/courses")
    @Operation(summary = "Ajouter plusieurs cours à une cohorte (ADMIN)")
    public CourseSessionDTO addCourses(@PathVariable Long sessionId,
                                       @Valid @RequestBody AddCoursesToSessionRequest request) {
        return service.addCourses(sessionId, request);
    }

    @GetMapping("/course-sessions/{sessionId}/candidates")
    @Operation(summary = "Lister les apprenants disponibles et inscrits d'une session (ADMIN)")
    public EnrollmentCandidatesDTO getCandidates(@PathVariable Long sessionId) {
        return service.getCandidates(sessionId);
    }

    @PostMapping("/course-sessions/{sessionId}/learners")
    @Operation(summary = "Inscrire plusieurs apprenants à une session (ADMIN)")
    public EnrollmentBatchResult enroll(@PathVariable Long sessionId,
                                        @Valid @RequestBody EnrollmentBatchRequest request) {
        return service.enroll(sessionId, request.userIds());
    }
}
