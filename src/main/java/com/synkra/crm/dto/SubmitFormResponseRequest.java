package com.synkra.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitFormResponseRequest(
    @Valid @NotEmpty List<FormAnswerInput> answers
) {
}
