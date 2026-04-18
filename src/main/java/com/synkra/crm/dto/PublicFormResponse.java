package com.synkra.crm.dto;

import java.util.List;

public record PublicFormResponse(
    String name,
    String slug,
    String headline,
    String description,
    String submitLabel,
    String successTitle,
    String successMessage,
    List<PublicFormQuestionResponse> questions
) {
}
