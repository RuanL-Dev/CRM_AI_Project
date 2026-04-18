package com.synkra.crm.repository;

import com.synkra.crm.model.EmailCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
}
