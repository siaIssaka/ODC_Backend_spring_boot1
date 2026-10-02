package com.example.ODC_Academy.assignment;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.model.quiz.QuizAttempt;
import com.example.ODC_Academy.progress.ProgressRepository;
import com.example.ODC_Academy.quiz.QuizAttemptRepository;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.security.SecurityUtils;
import com.example.ODC_Academy.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Suivi par apprenant d'un cours : leçons, quiz, devoirs. Réservé à l'admin et au formateur du cours. */
@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {
    public record LearnerTracking(Long userId, String name, String email, String photoKey, double percentage,
                                  int completedLessons, int totalLessons, Double quizAverage, int quizAttempts,
                                  int assignmentsTotal, int assignmentsSubmitted, Double assignmentAverage) { }

    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final ProgressRepository progress;
    private final QuizAttemptRepository quizAttempts;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;
    private final SecurityUtils security;

    public TrackingController(CourseRepository c, EnrollmentRepository e, ProgressRepository p, QuizAttemptRepository q,
                              AssignmentRepository a, SubmissionRepository s, SecurityUtils u) {
        this.courses = c; this.enrollments = e; this.progress = p; this.quizAttempts = q;
        this.assignments = a; this.submissions = s; this.security = u;
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMIN')")
    @Transactional(readOnly = true)
    public List<LearnerTracking> course(@PathVariable Long courseId) {
        User me = security.getCurrentUser();
        Course course = courses.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        if (!CourseAccess.manages(me, course)) throw new AccessDeniedException("Vous ne gérez pas ce cours");
        List<Long> assignmentIds = assignments.findByCourseIdOrderByClosesAtAsc(courseId).stream().map(Assignment::getId).toList();
        return enrollments.findByCourseId(courseId).stream().map(e -> row(e, courseId, assignmentIds)).toList();
    }

    private LearnerTracking row(Enrollment e, Long courseId, List<Long> assignmentIds) {
        User u = e.getUser();
        var p = progress.findByUserIdAndCourseId(u.getId(), courseId).orElse(null);
        List<QuizAttempt> qa = quizAttempts.findByUserIdAndQuiz_Lesson_Course_Id(u.getId(), courseId);
        List<Submission> subs = submissions.findByLearnerId(u.getId()).stream()
                .filter(s -> assignmentIds.contains(s.getAssignment().getId())).toList();
        return new LearnerTracking(u.getId(), u.getPrenom() + " " + u.getNom(), u.getEmail(), u.getPhotoKey(),
                p == null ? 0 : p.getPercentage(), p == null ? 0 : p.getCompletedLessons(), p == null ? 0 : p.getTotalLessons(),
                qa.isEmpty() ? null : qa.stream().mapToInt(QuizAttempt::getScore).average().orElse(0), qa.size(),
                assignmentIds.size(), subs.size(),
                subs.stream().filter(s -> s.getGrade() != null).mapToDouble(Submission::getGrade).average().stream().boxed().findFirst().orElse(null));
    }
}
