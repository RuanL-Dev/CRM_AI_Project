package com.synkra.crm.dto;

import com.synkra.crm.model.FormQuestion;
import com.synkra.crm.model.FormQuestionType;

import java.util.List;

public record FormQuestionResponse(
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

    public static FormQuestionResponse from(FormQuestion question, List<String> options) {
        return new FormQuestionResponse(
            question.getId(),
            question.getFieldKey(),
            question.getLabel(),
            question.getDescription(),
            question.getPlaceholder(),
            question.getQuestionType(),
            question.isRequired(),
            question.getPositionIndex(),
            options
        );
    }
}
