package com.synkra.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FormAnswerInput(
    @NotNull Long questionId,
    @NotBlank String value
) {
}
