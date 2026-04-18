package com.synkra.crm.dto;

import com.synkra.crm.model.CampaignDelivery;
import com.synkra.crm.model.CampaignDeliveryStatus;

import java.time.Instant;

public record CampaignDeliveryResponse(
    Long id,
    ContactSummaryResponse contact,
    CampaignDeliveryStatus status,
    String failureReason,
    Instant sentAt,
    Instant createdAt
) {

    public static CampaignDeliveryResponse from(CampaignDelivery delivery) {
        return new CampaignDeliveryResponse(
            delivery.getId(),
            ContactSummaryResponse.from(delivery.getContact()),
            delivery.getStatus(),
            delivery.getFailureReason(),
            delivery.getSentAt(),
            delivery.getCreatedAt()
        );
    }
}
