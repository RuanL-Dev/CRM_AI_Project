package com.synkra.crm.repository;

import com.synkra.crm.model.ContactStatus;

public interface ContactStatusCountView {

    ContactStatus getStatus();

    long getTotal();
}
