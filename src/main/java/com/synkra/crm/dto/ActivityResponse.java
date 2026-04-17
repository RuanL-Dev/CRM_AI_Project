package com.synkra.crm.dto;

import com.synkra.crm.model.Activity;
import com.synkra.crm.model.ActivityType;

import java.time.Instant;

public record ActivityResponse(
    Long id,
    ActivityType type,
    String notes,
    Instant dueAt,
    boolean completed,
    ContactSummaryResponse contact,
    Instant createdAt
) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getType(),
            activity.getNotes(),
            activity.getDueAt(),
            activity.isCompleted(),
            ContactSummaryResponse.from(activity.getContact()),
            activity.getCreatedAt()
        );
    }
}
