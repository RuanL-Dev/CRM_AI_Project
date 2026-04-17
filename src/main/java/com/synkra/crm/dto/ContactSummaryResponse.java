package com.synkra.crm.dto;

import com.synkra.crm.model.Contact;
import com.synkra.crm.model.ContactStatus;

public record ContactSummaryResponse(
    Long id,
    String name,
    String email,
    String company,
    ContactStatus status
) {

    public static ContactSummaryResponse from(Contact contact) {
        return new ContactSummaryResponse(
            contact.getId(),
            contact.getName(),
            contact.getEmail(),
            contact.getCompany(),
            contact.getStatus()
        );
    }
}
