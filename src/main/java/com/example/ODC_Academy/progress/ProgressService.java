package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.status.ProgressStatus;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ProgressService(ProgressRepository progressRepository,
                            UserRepository userRepository,
                            CourseRepository courseRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public List<Progress> getProgressByUser(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    public Optional<Progress> getProgress(Long userId, Long courseId) {
        return progressRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public Progress createProgress(Long userId, Long courseId) {
        Optional<Progress> existing = progressRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));

        Progress progress = Progress.builder()
                .user(user)
                .course(course)
                .completedLessons(0)
                .totalLessons(0)
                .percentage(0.0)
                .status(ProgressStatus.NOT_STARTED)
                .build();

        return progressRepository.save(progress);
    }

    public Progress updateProgress(Long userId, Long courseId, int completedLessons, int totalLessons) {
        Progress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> createProgress(userId, courseId));

        progress.setCompletedLessons(completedLessons);
        progress.setTotalLessons(totalLessons);
        progress.setPercentage(computePercentage(completedLessons, totalLessons));
        progress.setStatus(computeStatus(completedLessons, totalLessons));

        return progressRepository.save(progress);
    }

    private double computePercentage(int completedLessons, int totalLessons) {
        if (totalLessons <= 0) {
            return 0.0;
        }
        double pct = (completedLessons * 100.0) / totalLessons;
        return Math.round(pct * 100.0) / 100.0;
    }

    private ProgressStatus computeStatus(int completedLessons, int totalLessons) {
        if (completedLessons <= 0) {
            return ProgressStatus.NOT_STARTED;
        }
        if (totalLessons > 0 && completedLessons >= totalLessons) {
            return ProgressStatus.COMPLETED;
        }
        return ProgressStatus.IN_PROGRESS;
    }
}
