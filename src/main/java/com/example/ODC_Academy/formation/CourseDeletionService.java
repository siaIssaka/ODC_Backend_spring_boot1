package com.example.ODC_Academy.formation;

import com.example.ODC_Academy.media.MediaStorageService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseDeletionService {
    private final JdbcTemplate jdbc;
    private final MediaStorageService storage;

    public CourseDeletionService(JdbcTemplate jdbc, MediaStorageService storage) {
        this.jdbc = jdbc;
        this.storage = storage;
    }

    @Transactional
    public void deleteCourseContent(Long courseId) {
        deleteCourse(courseId);
    }

    @Transactional
    public void deleteFormationContent(Long formationId, String imageKey) {
        List<Long> courseIds = jdbc.query(
                "SELECT id FROM courses WHERE formation_id = ?",
                (result, row) -> result.getLong(1), formationId);
        for (Long courseId : courseIds) deleteCourse(courseId);

        jdbc.update("""
                WITH RECURSIVE related_posts(id) AS (
                    SELECT id FROM forum_posts WHERE formation_id = ?
                    UNION
                    SELECT reply.id FROM forum_posts reply
                    JOIN related_posts parent ON reply.parent_id = parent.id
                )
                DELETE FROM forum_posts WHERE id IN (SELECT id FROM related_posts)
                """, formationId);
        jdbc.update("DELETE FROM formation_trainers WHERE formation_id = ?", formationId);
        if (imageKey != null) storage.delete(imageKey);
        jdbc.update("DELETE FROM formations WHERE id = ?", formationId);
    }

    private void deleteCourse(Long courseId) {
        List<String> mediaUrls = jdbc.query(
                "SELECT video_url FROM lessons WHERE course_id = ? AND video_url IS NOT NULL "
                        + "UNION SELECT document_url FROM lessons WHERE course_id = ? AND document_url IS NOT NULL",
                (result, row) -> result.getString(1), courseId, courseId);
        List<String> submissionKeys = jdbc.query("""
                SELECT submission.file_key
                FROM submissions submission
                JOIN assignments assignment ON assignment.id = submission.assignment_id
                WHERE assignment.course_id = ?
                """, (result, row) -> result.getString(1), courseId);

        jdbc.update("""
                DELETE FROM quiz_attempts
                WHERE quiz_id IN (
                    SELECT quiz.id FROM quizzes quiz
                    JOIN lessons lesson ON lesson.id = quiz.lesson_id
                    WHERE lesson.course_id = ?
                )
                """, courseId);
        jdbc.update("""
                DELETE FROM answer_options
                WHERE question_id IN (
                    SELECT question.id FROM questions question
                    JOIN quizzes quiz ON quiz.id = question.quiz_id
                    JOIN lessons lesson ON lesson.id = quiz.lesson_id
                    WHERE lesson.course_id = ?
                )
                """, courseId);
        jdbc.update("""
                DELETE FROM questions
                WHERE quiz_id IN (
                    SELECT quiz.id FROM quizzes quiz
                    JOIN lessons lesson ON lesson.id = quiz.lesson_id
                    WHERE lesson.course_id = ?
                )
                """, courseId);
        jdbc.update("""
                DELETE FROM quizzes
                WHERE lesson_id IN (SELECT id FROM lessons WHERE course_id = ?)
                """, courseId);
        jdbc.update("""
                DELETE FROM lesson_completions
                WHERE lesson_id IN (SELECT id FROM lessons WHERE course_id = ?)
                """, courseId);
        jdbc.update("""
                DELETE FROM submissions
                WHERE assignment_id IN (SELECT id FROM assignments WHERE course_id = ?)
                """, courseId);
        jdbc.update("DELETE FROM assignments WHERE course_id = ?", courseId);
        jdbc.update("DELETE FROM live_sessions WHERE course_id = ?", courseId);
        jdbc.update("DELETE FROM progress WHERE course_id = ?", courseId);
        jdbc.update("DELETE FROM enrollments WHERE course_id = ?", courseId);
        jdbc.update("DELETE FROM lessons WHERE course_id = ?", courseId);
        jdbc.update("DELETE FROM modules WHERE course_id = ?", courseId);
        for (String url : mediaUrls) storage.deleteMediaUrl(url);
        for (String key : submissionKeys) storage.delete(key);
        jdbc.update("DELETE FROM courses WHERE id = ?", courseId);
    }
}
