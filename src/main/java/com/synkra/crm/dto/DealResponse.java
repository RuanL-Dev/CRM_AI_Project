package com.synkra.crm.dto;

import com.synkra.crm.model.Deal;
import com.synkra.crm.model.DealStage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DealResponse(
    Long id,
    String title,
    BigDecimal value,
    DealStage stage,
    LocalDate expectedCloseDate,
    ContactSummaryResponse contact,
    Instant createdAt
) {

    public static DealResponse from(Deal deal) {
        return new DealResponse(
            deal.getId(),
            deal.getTitle(),
            deal.getValue(),
            deal.getStage(),
            deal.getExpectedCloseDate(),
            ContactSummaryResponse.from(deal.getContact()),
            deal.getCreatedAt()
        );
    }
}
