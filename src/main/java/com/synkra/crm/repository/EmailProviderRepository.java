package com.synkra.crm.repository;

import com.synkra.crm.model.EmailProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailProviderRepository extends JpaRepository<EmailProvider, Long> {
}
