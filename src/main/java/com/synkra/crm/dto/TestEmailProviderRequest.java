package com.synkra.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TestEmailProviderRequest(
    @NotBlank @Email String recipientEmail
) {
}
