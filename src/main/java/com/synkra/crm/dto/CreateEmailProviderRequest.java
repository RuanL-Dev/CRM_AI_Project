package com.synkra.crm.dto;

import com.synkra.crm.model.EmailProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmailProviderRequest(
    @NotBlank String name,
    @NotNull EmailProviderType providerType,
    @NotBlank String host,
    @NotNull @Min(1) @Max(65535) Integer port,
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank @Email String fromEmail,
    String fromName,
    @Email String replyTo,
    boolean tlsEnabled,
    boolean active
) {
}
