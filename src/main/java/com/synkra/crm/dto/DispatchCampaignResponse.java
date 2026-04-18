package com.synkra.crm.dto;

public record DispatchCampaignResponse(
    Long campaignId,
    int recipients,
    int sent,
    int failed
) {
}
