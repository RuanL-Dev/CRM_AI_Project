package com.synkra.crm.dto;

import com.synkra.crm.model.ActivityType;
import com.synkra.crm.model.ContactStatus;
import com.synkra.crm.model.DealStage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardMetricsResponse(
    long contacts,
    long deals,
    long activities,
    long totalRecords,
    long overdueActivities,
    long scheduledActivities,
    BigDecimal projectedRevenue,
    BigDecimal closedRevenue,
    BigDecimal conversionRate,
    Map<DealStage, BigDecimal> pipelineByStage,
    Map<ContactStatus, Long> leadStatusCounts,
    Map<ActivityType, Long> activityTypeCounts,
    List<DashboardTimelineItemResponse> recordsTimeline
) {
}
