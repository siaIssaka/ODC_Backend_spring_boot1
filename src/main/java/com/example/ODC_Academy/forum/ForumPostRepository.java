package com.example.ODC_Academy.forum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByFormationIdAndParentIsNullOrderByCreatedAtDesc(Long formationId);
    List<ForumPost> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByParentId(Long parentId);
}
