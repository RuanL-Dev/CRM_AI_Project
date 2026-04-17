package com.synkra.crm.dto;

public record DashboardTimelineItemResponse(
    String date,
    String label,
    long leads,
    long oportunidades,
    long atividades,
    long total
) {
}
