package com.synkra.crm.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateSegmentRequest(
    @NotBlank String name,
    String description,
    String color,
    List<Long> contactIds
) {
}
