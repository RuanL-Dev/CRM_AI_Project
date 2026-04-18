package com.synkra.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateFormRequest(
    @NotBlank String name,
    @NotBlank String slug,
    @NotBlank String headline,
    String description,
    @NotBlank String submitLabel,
    @NotBlank String successTitle,
    @NotBlank String successMessage,
    boolean active,
    Long targetSegmentId,
    @Valid @NotEmpty List<FormQuestionInput> questions
) {
}
