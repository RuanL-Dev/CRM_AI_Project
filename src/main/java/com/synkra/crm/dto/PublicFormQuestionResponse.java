package com.synkra.crm.dto;

import com.synkra.crm.model.FormQuestionType;

import java.util.List;

public record PublicFormQuestionResponse(
    Long id,
    String fieldKey,
    String label,
    String description,
    String placeholder,
    FormQuestionType questionType,
    boolean required,
    Integer positionIndex,
    List<String> options
) {
}
