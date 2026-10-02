package com.example.ODC_Academy.lesson;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.model.quiz.Quiz;
import com.example.ODC_Academy.module.Module;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    @Column
    private String videoUrl;

    @Column
    private String documentUrl;

    @Column
    private Integer durationMinutes;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;
}
