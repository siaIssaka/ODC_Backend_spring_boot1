package com.example.ODC_Academy.liveclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class LiveClassDtos {
    private LiveClassDtos() { }

    public record CreateRequest(@NotBlank String title, @NotNull LocalDateTime scheduledAt) { }

    public record SessionDto(Long id, Long courseId, String title, String roomName,
                             LocalDateTime scheduledAt, LiveSessionStatus status,
                             LocalDateTime startedAt, LocalDateTime endedAt) { }
}
