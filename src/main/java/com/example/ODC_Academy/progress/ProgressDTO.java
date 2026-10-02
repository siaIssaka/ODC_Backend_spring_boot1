package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.status.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private int completedLessons;
    private int totalLessons;
    private double percentage;
    private ProgressStatus status;
    private LocalDateTime updatedAt;
}
