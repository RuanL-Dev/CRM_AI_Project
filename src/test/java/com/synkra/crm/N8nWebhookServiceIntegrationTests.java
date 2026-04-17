package com.synkra.crm;

import com.synkra.crm.model.WebhookDelivery;
import com.synkra.crm.model.WebhookDeliveryStatus;
import com.synkra.crm.repository.WebhookDeliveryRepository;
import com.synkra.crm.service.N8nWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "n8n.webhook-url=http://localhost/test-webhook")
class N8nWebhookServiceIntegrationTests {

    @Autowired
    private N8nWebhookService n8nWebhookService;

    @Autowired
    private WebhookDeliveryRepository webhookDeliveryRepository;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void clearDeliveries() {
        webhookDeliveryRepository.deleteAll();
    }

    @Test
    void publishPersistsSentDeliveryWhenWebhookSucceeds() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class)))
            .thenReturn(ResponseEntity.ok().build());

        n8nWebhookService.publish("contact.created", Map.of("id", 1L, "name", "Maria"));

        WebhookDelivery delivery = webhookDeliveryRepository.findAll().get(0);
        assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.SENT);
        assertThat(delivery.getAttemptCount()).isEqualTo(1);
        assertThat(delivery.getSentAt()).isNotNull();
        assertThat(delivery.getNextAttemptAt()).isNull();
        assertThat(delivery.getPayloadJson()).contains("\"id\":1");
    }

    @Test
    void failedDeliveryIsPersistedAndCanBeRetried() {
        doThrow(new RuntimeException("n8n offline"))
            .when(restTemplate)
            .postForEntity(any(String.class), any(), eq(Void.class));

        n8nWebhookService.publish("deal.created", Map.of("id", 10L));

        WebhookDelivery failedDelivery = webhookDeliveryRepository.findAll().get(0);
        assertThat(failedDelivery.getStatus()).isEqualTo(WebhookDeliveryStatus.FAILED);
        assertThat(failedDelivery.getAttemptCount()).isEqualTo(1);
        assertThat(failedDelivery.getLastError()).contains("n8n offline");
        assertThat(failedDelivery.getNextAttemptAt()).isNotNull();

        failedDelivery.setNextAttemptAt(Instant.now().minusSeconds(1));
        webhookDeliveryRepository.save(failedDelivery);

        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class)))
            .thenReturn(ResponseEntity.ok().build());

        n8nWebhookService.attemptDelivery(failedDelivery.getId());

        WebhookDelivery retriedDelivery = webhookDeliveryRepository.findById(failedDelivery.getId()).orElseThrow();
        assertThat(retriedDelivery.getStatus()).isEqualTo(WebhookDeliveryStatus.SENT);
        assertThat(retriedDelivery.getAttemptCount()).isEqualTo(2);
        assertThat(retriedDelivery.getSentAt()).isNotNull();
        assertThat(retriedDelivery.getLastError()).isNull();
    }
}
