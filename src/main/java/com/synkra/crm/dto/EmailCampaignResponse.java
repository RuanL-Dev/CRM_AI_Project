package com.synkra.crm.dto;

import com.synkra.crm.model.EmailCampaign;
import com.synkra.crm.model.EmailCampaignStatus;

import java.time.Instant;
import java.util.List;

public record EmailCampaignResponse(
    Long id,
    String name,
    String subject,
    String previewText,
    String senderName,
    String htmlContent,
    String plainTextContent,
    EmailCampaignStatus status,
    EmailProviderResponse provider,
    List<SegmentResponse> segments,
    List<CampaignDeliveryResponse> deliveries,
    Instant createdAt,
    Instant updatedAt
) {

    public static EmailCampaignResponse from(EmailCampaign campaign, List<CampaignDeliveryResponse> deliveries) {
        return new EmailCampaignResponse(
            campaign.getId(),
            campaign.getName(),
            campaign.getSubject(),
            campaign.getPreviewText(),
            campaign.getSenderName(),
            campaign.getHtmlContent(),
            campaign.getPlainTextContent(),
            campaign.getStatus(),
            campaign.getProvider() == null ? null : EmailProviderResponse.from(campaign.getProvider()),
            campaign.getSegments().stream()
                .map(SegmentResponse::from)
                .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                .toList(),
            deliveries,
            campaign.getCreatedAt(),
            campaign.getUpdatedAt()
        );
    }
}
