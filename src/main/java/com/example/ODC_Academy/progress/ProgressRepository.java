package com.example.ODC_Academy.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByUserIdAndCourseId(Long userId, Long courseId);
    List<Progress> findByUserId(Long userId);
    List<Progress> findByCourseId(Long courseId);
}
