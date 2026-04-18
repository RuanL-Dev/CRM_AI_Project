package com.synkra.crm.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateSegmentContactsRequest(
    @NotNull List<Long> contactIds
) {
}
