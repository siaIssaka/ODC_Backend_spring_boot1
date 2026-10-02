package com.example.ODC_Academy.progress;

import com.example.ODC_Academy.progress.ProgressDTO;

public final class ProgressMapper {

    private ProgressMapper() {
    }

    public static ProgressDTO toDto(Progress progress) {
        return new ProgressDTO(
                progress.getId(),
                progress.getUser().getId(),
                progress.getCourse().getId(),
                progress.getCompletedLessons(),
                progress.getTotalLessons(),
                progress.getPercentage(),
                progress.getStatus(),
                progress.getUpdatedAt()
        );
    }
}
