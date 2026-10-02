package com.example.ODC_Academy.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
    List<LessonCompletion> findByUserIdAndLessonCourseId(Long userId, Long courseId);
    long countByUserIdAndLessonCourseId(Long userId, Long courseId);
}
