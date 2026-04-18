package com.synkra.crm.dto;

public record SubmitFormResponseResult(
    Long responseId,
    Long contactId,
    Long targetSegmentId
) {
}
