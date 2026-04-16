package com.synkra.crm.dto;

import com.synkra.crm.model.ContactStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateContactRequest(
    @NotBlank(message = "nome: informe o nome do contato") String name,
    @Email(message = "email: informe um e-mail válido")
    @NotBlank(message = "email: informe o e-mail do contato") String email,
    String phone,
    String company,
    ContactStatus status
) {
}
