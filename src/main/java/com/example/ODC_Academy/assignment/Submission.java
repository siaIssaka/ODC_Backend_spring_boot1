package com.example.ODC_Academy.assignment;

import com.example.ODC_Academy.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "learner_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id")
    private User learner;
    @Column(nullable = false)
    private String fileKey;
    private String originalName;
    @Column(nullable = false)
    private LocalDateTime submittedAt;
    private Double grade;
    @Column(length = 2000)
    private String feedback;
}
