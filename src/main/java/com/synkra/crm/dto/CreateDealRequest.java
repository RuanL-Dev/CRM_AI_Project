package com.synkra.crm.dto;

import com.synkra.crm.model.DealStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDealRequest(
    @NotBlank(message = "titulo: informe o nome da oportunidade") String title,
    @NotNull(message = "valor: informe o valor da oportunidade")
    @DecimalMin(value = "0.0", message = "valor: informe um valor igual ou maior que zero") BigDecimal value,
    DealStage stage,
    LocalDate expectedCloseDate,
    @NotNull(message = "contato: selecione um contato para a oportunidade") Long contactId
) {
}
