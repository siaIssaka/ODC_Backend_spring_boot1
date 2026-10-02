package com.example.ODC_Academy.enrollment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EnrollmentBatchRequest(
        @NotEmpty List<@NotNull @Positive Long> userIds
) {
}
