package com.example.ODC_Academy.liveclass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface LiveClassRepository extends JpaRepository<LiveClassSession, Long> {
    List<LiveClassSession> findByCourseIdOrderByScheduledAtAsc(Long courseId);
}
