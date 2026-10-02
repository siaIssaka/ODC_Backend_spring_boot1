package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
public class LessonProgressController {
    private final LessonProgressService service;
    private final SecurityUtils security;

    public LessonProgressController(LessonProgressService s, SecurityUtils u) { this.service = s; this.security = u; }

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('APPRENANT')")
    public void complete(@PathVariable Long lessonId) { service.complete(lessonId, security.getCurrentUser()); }

    @GetMapping("/course/{courseId}/completed")
    @PreAuthorize("hasRole('APPRENANT')")
    public List<Long> completed(@PathVariable Long courseId) {
        return service.completedLessonIds(security.getCurrentUser().getId(), courseId);
    }
}
