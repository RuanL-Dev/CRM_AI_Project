package com.synkra.crm.dto;

import com.synkra.crm.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateActivityRequest(
    @NotNull(message = "tipo: selecione o tipo da atividade") ActivityType type,
    @NotBlank(message = "notas: descreva a atividade") String notes,
    Instant dueAt,
    @NotNull(message = "contato: selecione um contato para a atividade") Long contactId
) {
}
