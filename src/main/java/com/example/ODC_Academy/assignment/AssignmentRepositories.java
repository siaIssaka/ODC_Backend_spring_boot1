package com.example.ODC_Academy.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdOrderByClosesAtAsc(Long courseId);
    List<Assignment> findByCourseIdInOrderByClosesAtAsc(List<Long> courseIds);
}

interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentIdAndLearnerId(Long assignmentId, Long learnerId);
    List<Submission> findByAssignmentId(Long assignmentId);
    long countByAssignmentId(Long assignmentId);
    List<Submission> findByLearnerId(Long learnerId);
}
