package com.synkra.crm.dto;

import com.synkra.crm.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateActivityRequest(
    @NotNull ActivityType type,
    @NotBlank String notes,
    Instant dueAt,
    @NotNull Long contactId
) {
}
