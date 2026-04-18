package com.synkra.crm.repository;

import com.synkra.crm.model.CampaignDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignDeliveryRepository extends JpaRepository<CampaignDelivery, Long> {
    List<CampaignDelivery> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
}
