package com.example.ODC_Academy.forum;

import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Fil de discussion (parent == null, avec titre) ou réponse (parent != null). */
@Entity
@Table(name = "forum_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "formation_id")
    private Formation formation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id")
    private User author;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id")
    private ForumPost parent;
    private String title;
    @Column(nullable = false, length = 4000)
    private String body;
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
