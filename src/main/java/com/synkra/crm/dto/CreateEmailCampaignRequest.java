package com.synkra.crm.dto;

import com.synkra.crm.model.EmailCampaignStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateEmailCampaignRequest(
    @NotBlank String name,
    @NotBlank String subject,
    String previewText,
    String senderName,
    @NotBlank String htmlContent,
    String plainTextContent,
    EmailCampaignStatus status,
    Long providerId,
    @NotEmpty List<Long> segmentIds
) {
}
