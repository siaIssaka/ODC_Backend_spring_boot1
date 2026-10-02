package com.example.ODC_Academy.liveclass;

import com.example.ODC_Academy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.ODC_Academy.liveclass.LiveClassDtos.*;

@RestController
@RequestMapping("/api/v1/live-sessions")
public class LiveClassController {
    private final LiveClassService service;
    private final SecurityUtils security;

    public LiveClassController(LiveClassService service, SecurityUtils security) {
        this.service = service;
        this.security = security;
    }

    @GetMapping("/course/{courseId}")
    public List<SessionDto> forCourse(@PathVariable Long courseId) {
        return service.forCourse(courseId, security.getCurrentUser());
    }

    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionDto schedule(@PathVariable Long courseId, @Valid @RequestBody CreateRequest request) {
        return service.schedule(courseId, request, security.getCurrentUser());
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionDto start(@PathVariable Long id) {
        return service.start(id, security.getCurrentUser());
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionDto publish(@PathVariable Long id) {
        return service.publish(id, security.getCurrentUser());
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionDto end(@PathVariable Long id) {
        return service.end(id, security.getCurrentUser());
    }
}
