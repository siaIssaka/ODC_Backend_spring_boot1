package com.example.ODC_Academy.coursesession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
    List<CourseSession> findByFormationIdOrderByStartsAtAsc(Long formationId);
}
