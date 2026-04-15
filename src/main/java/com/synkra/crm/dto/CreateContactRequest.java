package com.synkra.crm.dto;

import com.synkra.crm.model.ContactStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateContactRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    String phone,
    String company,
    ContactStatus status
) {
}
