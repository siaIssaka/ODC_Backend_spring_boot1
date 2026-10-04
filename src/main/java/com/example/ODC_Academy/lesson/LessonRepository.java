package com.example.ODC_Academy.lesson;

import com.example.ODC_Academy.lesson.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    Optional<Lesson> findByVideoUrlOrDocumentUrl(String videoUrl, String documentUrl);
}
