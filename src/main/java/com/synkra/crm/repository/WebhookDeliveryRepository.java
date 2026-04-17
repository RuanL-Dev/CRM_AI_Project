package com.synkra.crm.repository;

import com.synkra.crm.model.WebhookDelivery;
import com.synkra.crm.model.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
        Collection<WebhookDeliveryStatus> statuses,
        Instant nextAttemptAt
    );
}
