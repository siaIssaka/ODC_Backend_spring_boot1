package com.example.ODC_Academy.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<com.example.ODC_Academy.model.quiz.Quiz, Long> {
    Optional<com.example.ODC_Academy.model.quiz.Quiz> findByLessonId(Long lessonId);
}
