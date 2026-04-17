package com.synkra.crm.dto;

import com.synkra.crm.model.Contact;
import com.synkra.crm.model.ContactStatus;

import java.time.Instant;

public record ContactResponse(
    Long id,
    String name,
    String email,
    String phone,
    String company,
    ContactStatus status,
    Instant createdAt
) {

    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
            contact.getId(),
            contact.getName(),
            contact.getEmail(),
            contact.getPhone(),
            contact.getCompany(),
            contact.getStatus(),
            contact.getCreatedAt()
        );
    }
}
