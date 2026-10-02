package com.example.ODC_Academy.assignment;

import com.example.ODC_Academy.course.Course;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Espace de dépôt : ouvert entre opensAt et closesAt, fermé automatiquement ensuite (vérifié côté serveur). */
@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
    @Column(nullable = false)
    private LocalDateTime opensAt;
    @Column(nullable = false)
    private LocalDateTime closesAt;

    public String status(LocalDateTime now) {
        if (now.isBefore(opensAt)) return "PLANIFIE";
        return now.isBefore(closesAt) ? "OUVERT" : "FERME";
    }
}
