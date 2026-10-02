package com.example.ODC_Academy.quiz;

import com.example.ODC_Academy.model.quiz.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public final class QuizDtos {
    private QuizDtos() { }

    public record OptionRequest(@NotBlank String label, boolean correct) { }
    public record QuestionRequest(@NotBlank String prompt, @NotNull QuestionType type,
                                  @NotEmpty @Valid List<OptionRequest> options) { }
    public record QuizRequest(@NotBlank String title, String description, @Min(0) @Max(100) int passingScore,
                              @NotEmpty @Valid List<QuestionRequest> questions) { }

    /** correct est null pour un apprenant : la bonne réponse n'est jamais envoyée avant correction. */
    public record OptionView(Long id, String label, Boolean correct) { }
    public record QuestionView(Long id, String prompt, QuestionType type, List<OptionView> options) { }
    public record QuizView(Long id, Long lessonId, String title, String description, int passingScore,
                           List<QuestionView> questions) { }

    public record AttemptRequest(@NotNull Map<Long, List<Long>> answers) { }
    public record AttemptResult(int score, boolean passed, int passingScore, int correct, int total,
                                List<Long> wrongQuestionIds) { }
}
