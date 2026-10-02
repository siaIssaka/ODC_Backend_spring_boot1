package com.example.ODC_Academy.assignment;

import com.example.ODC_Academy.assignment.AssignmentDtos.*;
import com.example.ODC_Academy.media.MediaStorageService;
import com.example.ODC_Academy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {
    private final AssignmentService service;
    private final SecurityUtils security;
    private final MediaStorageService storage;

    public AssignmentController(AssignmentService s, SecurityUtils sec, MediaStorageService m) {
        this.service = s; this.security = sec; this.storage = m;
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('APPRENANT')")
    public List<AssignmentDto> mine() { return service.mine(security.getCurrentUser()); }

    @GetMapping("/course/{courseId}")
    public List<AssignmentDto> forCourse(@PathVariable Long courseId) {
        return service.forCourse(courseId, security.getCurrentUser());
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentDto create(@PathVariable Long courseId, @Valid @RequestBody AssignmentRequest r) {
        return service.create(courseId, r, security.getCurrentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public AssignmentDto update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest r) {
        return service.update(id, r, security.getCurrentUser());
    }

    @PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('APPRENANT')")
    public AssignmentDto submit(@PathVariable Long id,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam(value = "text", required = false) String text) {
        return service.submit(id, file, text, security.getCurrentUser());
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMIN')")
    public List<SubmissionDto> submissions(@PathVariable Long id) {
        return service.submissionsOf(id, security.getCurrentUser());
    }

    @PatchMapping("/submissions/{id}/grade")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMIN')")
    public void grade(@PathVariable Long id, @Valid @RequestBody GradeRequest r) {
        service.grade(id, r, security.getCurrentUser());
    }

    @GetMapping("/submissions/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        Submission s = service.fileOf(id, security.getCurrentUser());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.contentType(s.getFileKey())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + s.getFileKey() + "\"")
                .body(storage.load(s.getFileKey()));
    }
}
