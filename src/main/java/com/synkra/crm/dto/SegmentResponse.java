package com.synkra.crm.dto;

import com.synkra.crm.model.Segment;

import java.time.Instant;
import java.util.List;

public record SegmentResponse(
    Long id,
    String name,
    String description,
    String color,
    int contactCount,
    List<ContactSummaryResponse> contacts,
    Instant createdAt
) {

    public static SegmentResponse from(Segment segment) {
        return new SegmentResponse(
            segment.getId(),
            segment.getName(),
            segment.getDescription(),
            segment.getColor(),
            segment.getContacts().size(),
            segment.getContacts().stream()
                .map(ContactSummaryResponse::from)
                .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                .toList(),
            segment.getCreatedAt()
        );
    }
}
