package com.example.ODC_Academy.quiz;

import com.example.ODC_Academy.model.quiz.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdAndQuiz_Lesson_Course_Id(Long userId, Long courseId);
}
