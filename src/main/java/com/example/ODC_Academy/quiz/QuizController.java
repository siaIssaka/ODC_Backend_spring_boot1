package com.example.ODC_Academy.quiz;

import com.example.ODC_Academy.quiz.QuizDtos.*;
import com.example.ODC_Academy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {
    private final QuizService service;
    private final SecurityUtils security;

    public QuizController(QuizService s, SecurityUtils u) { this.service = s; this.security = u; }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<QuizView> get(@PathVariable Long lessonId) {
        return service.forLesson(lessonId, security.getCurrentUser())
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMIN')")
    public QuizView save(@PathVariable Long lessonId, @Valid @RequestBody QuizRequest r) {
        return service.save(lessonId, r, security.getCurrentUser());
    }

    @PostMapping("/{quizId}/attempts")
    @PreAuthorize("hasRole('APPRENANT')")
    public AttemptResult attempt(@PathVariable Long quizId, @Valid @RequestBody AttemptRequest r) {
        return service.attempt(quizId, r, security.getCurrentUser());
    }
}
