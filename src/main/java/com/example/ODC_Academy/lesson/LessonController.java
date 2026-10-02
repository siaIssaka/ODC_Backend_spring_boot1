package com.example.ODC_Academy.lesson;

import com.example.ODC_Academy.lesson.LessonDTO;
import com.example.ODC_Academy.lesson.LessonRequestDTO;
import com.example.ODC_Academy.lesson.LessonMapper;
import com.example.ODC_Academy.lesson.Lesson;
import com.example.ODC_Academy.lesson.LessonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@Tag(name = "Leçons", description = "Leçons rattachées à un cours")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/course/{courseId}")
    public List<LessonDTO> getLessonsByCourse(@PathVariable Long courseId) {
        return lessonService.getLessonsByCourse(courseId).stream().map(LessonMapper::toDto).toList();
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<LessonDTO> createLesson(@PathVariable Long courseId,
                                                   @Valid @RequestBody LessonRequestDTO request) {
        Lesson created = lessonService.createLesson(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LessonMapper.toDto(created));
    }

    @PostMapping(value = "/course/{courseId}/with-files", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    public ResponseEntity<LessonDTO> createLessonWithFiles(
            @PathVariable Long courseId,
            @RequestPart("lesson") @Valid LessonRequestDTO request,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        Lesson created = lessonService.createLessonWithFiles(courseId, request, video, document);
        return ResponseEntity.status(HttpStatus.CREATED).body(LessonMapper.toDto(created));
    }
}
