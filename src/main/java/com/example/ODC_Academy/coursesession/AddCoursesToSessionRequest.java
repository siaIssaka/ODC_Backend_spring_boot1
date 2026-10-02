package com.example.ODC_Academy.coursesession;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddCoursesToSessionRequest(@NotEmpty List<@NotNull Long> courseIds) {
}
