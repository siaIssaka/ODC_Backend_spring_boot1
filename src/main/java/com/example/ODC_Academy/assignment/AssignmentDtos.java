package com.example.ODC_Academy.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class AssignmentDtos {
    private AssignmentDtos() { }

    public record AssignmentRequest(@NotBlank String title, String description,
                                    @NotNull LocalDateTime opensAt, @NotNull LocalDateTime closesAt) { }

    public record GradeRequest(@NotNull Double grade, String feedback) { }

    /** status = PLANIFIE | OUVERT | FERME ; submitted/grade renseignés pour l'apprenant connecté. */
    public record AssignmentDto(Long id, Long courseId, String courseTitle, String title, String description,
                                LocalDateTime opensAt, LocalDateTime closesAt, String status,
                                boolean submitted, Double grade) { }

    public record SubmissionDto(Long id, Long learnerId, String learnerName, String originalName,
                                LocalDateTime submittedAt, Double grade, String feedback) { }
}
