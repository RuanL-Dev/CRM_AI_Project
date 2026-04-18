package com.synkra.crm.dto;

import com.synkra.crm.model.FormQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FormQuestionInput(
    @NotBlank String fieldKey,
    @NotBlank String label,
    String description,
    String placeholder,
    @NotNull FormQuestionType questionType,
    boolean required,
    @NotNull Integer positionIndex,
    List<String> options
) {
}
