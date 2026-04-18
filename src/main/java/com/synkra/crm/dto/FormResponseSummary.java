package com.synkra.crm.dto;

import com.synkra.crm.model.FormResponse;

import java.time.Instant;

public record FormResponseSummary(
    Long id,
    String respondentName,
    String respondentEmail,
    String respondentPhone,
    Instant createdAt
) {

    public static FormResponseSummary from(FormResponse response) {
        return new FormResponseSummary(
            response.getId(),
            response.getRespondentName(),
            response.getRespondentEmail(),
            response.getRespondentPhone(),
            response.getCreatedAt()
        );
    }
}
