package com.synkra.crm.dto;

import com.synkra.crm.model.FormDefinition;

import java.time.Instant;
import java.util.List;

public record FormDefinitionResponse(
    Long id,
    String name,
    String slug,
    String headline,
    String description,
    String submitLabel,
    String successTitle,
    String successMessage,
    boolean active,
    SegmentResponse targetSegment,
    List<FormQuestionResponse> questions,
    List<FormResponseSummary> responses,
    Instant createdAt,
    Instant updatedAt
) {

    public static FormDefinitionResponse from(FormDefinition form,
                                              List<FormQuestionResponse> questions,
                                              List<FormResponseSummary> responses) {
        return new FormDefinitionResponse(
            form.getId(),
            form.getName(),
            form.getSlug(),
            form.getHeadline(),
            form.getDescription(),
            form.getSubmitLabel(),
            form.getSuccessTitle(),
            form.getSuccessMessage(),
            form.isActive(),
            form.getTargetSegment() == null ? null : SegmentResponse.from(form.getTargetSegment()),
            questions,
            responses,
            form.getCreatedAt(),
            form.getUpdatedAt()
        );
    }
}
