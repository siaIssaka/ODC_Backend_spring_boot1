package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.lesson.Lesson;
import com.example.ODC_Academy.lesson.LessonRepository;
import com.example.ODC_Academy.status.ProgressStatus;
import com.example.ODC_Academy.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Marque une leçon terminée et recalcule la progression du cours (source de vérité : leçons réellement terminées). */
@Service
public class LessonProgressService {
    private final LessonRepository lessons;
    private final LessonCompletionRepository completions;
    private final ProgressRepository progress;
    private final EnrollmentRepository enrollments;

    public LessonProgressService(LessonRepository l, LessonCompletionRepository c, ProgressRepository p, EnrollmentRepository e) {
        this.lessons = l; this.completions = c; this.progress = p; this.enrollments = e;
    }

    @Transactional
    public void complete(Long lessonId, User learner) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> ResourceNotFoundException.of("Leçon", lessonId));
        completeInternal(lesson, learner);
    }

    /** Appelable depuis un autre service (ex. quiz réussi). */
    @Transactional
    public void completeInternal(Lesson lesson, User learner) {
        Long courseId = lesson.getCourse().getId();
        if (enrollments.findByUserIdAndCourseId(learner.getId(), courseId).isEmpty())
            throw new BadRequestException("Vous n'êtes pas inscrit à ce cours");
        if (!completions.existsByUserIdAndLessonId(learner.getId(), lesson.getId())) {
            completions.save(LessonCompletion.builder().user(learner).lesson(lesson).completedAt(LocalDateTime.now()).build());
        }
        int total = lessons.findByCourseIdOrderByOrderIndexAsc(courseId).size();
        int done = (int) completions.countByUserIdAndLessonCourseId(learner.getId(), courseId);
        Progress p = progress.findByUserIdAndCourseId(learner.getId(), courseId)
                .orElseGet(() -> Progress.builder().user(learner).course(lesson.getCourse()).build());
        p.setTotalLessons(total);
        p.setCompletedLessons(Math.min(done, total));
        p.setPercentage(total == 0 ? 0 : Math.round(1000.0 * Math.min(done, total) / total) / 10.0);
        p.setStatus(done >= total && total > 0 ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS);
        progress.save(p);
    }

    @Transactional(readOnly = true)
    public List<Long> completedLessonIds(Long userId, Long courseId) {
        return completions.findByUserIdAndLessonCourseId(userId, courseId).stream().map(c -> c.getLesson().getId()).toList();
    }
}
