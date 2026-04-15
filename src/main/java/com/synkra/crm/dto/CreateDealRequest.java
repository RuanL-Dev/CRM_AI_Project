package com.synkra.crm.dto;

import com.synkra.crm.model.DealStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDealRequest(
    @NotBlank String title,
    @NotNull @DecimalMin("0.0") BigDecimal value,
    DealStage stage,
    LocalDate expectedCloseDate,
    @NotNull Long contactId
) {
}
