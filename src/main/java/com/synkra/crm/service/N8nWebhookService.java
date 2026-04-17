package com.synkra.crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkra.crm.model.WebhookDelivery;
import com.synkra.crm.model.WebhookDeliveryStatus;
import com.synkra.crm.repository.WebhookDeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class N8nWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(N8nWebhookService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WebhookDeliveryRepository webhookDeliveryRepository;

    @Value("${n8n.retry-delay-ms:30000}")
    private long retryDelayMs;

    @Value("${n8n.max-attempts:5}")
    private int maxAttempts;

    @Value("${n8n.webhook-url:}")
    private String webhookUrl;

    public N8nWebhookService(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             WebhookDeliveryRepository webhookDeliveryRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
    }

    public void publish(String eventType, Object payload) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setEventType(eventType);
            delivery.setPayloadJson(objectMapper.writeValueAsString(payload));
            delivery.setStatus(WebhookDeliveryStatus.PENDING);
            delivery.setNextAttemptAt(Instant.now());

            WebhookDelivery saved = webhookDeliveryRepository.save(delivery);
            attemptDelivery(saved.getId());
        } catch (Exception ex) {
            LOGGER.error("Failed to register webhook delivery for event {}", eventType, ex);
        }
    }

    @Scheduled(fixedDelayString = "${n8n.retry-scheduler-delay-ms:30000}")
    public void retryPendingDeliveries() {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        List<Long> deliveryIds = webhookDeliveryRepository
            .findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                EnumSet.of(WebhookDeliveryStatus.PENDING, WebhookDeliveryStatus.FAILED),
                Instant.now()
            )
            .stream()
            .map(WebhookDelivery::getId)
            .toList();

        deliveryIds.forEach(this::attemptDelivery);
    }

    public void attemptDelivery(Long deliveryId) {
        WebhookDelivery delivery = webhookDeliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getStatus() == WebhookDeliveryStatus.SENT) {
            return;
        }

        if (delivery.getNextAttemptAt() != null && delivery.getNextAttemptAt().isAfter(Instant.now())) {
            return;
        }

        Map<String, Object> envelope = Map.of(
            "eventType", delivery.getEventType(),
            "sentAt", Instant.now().toString(),
            "payload", payloadNode(delivery)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(envelope, headers), Void.class);
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setStatus(WebhookDeliveryStatus.SENT);
            delivery.setLastError(null);
            delivery.setNextAttemptAt(null);
            delivery.setSentAt(Instant.now());
        } catch (Exception ex) {
            int attemptCount = delivery.getAttemptCount() + 1;
            delivery.setAttemptCount(attemptCount);
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setLastError(truncate(ex.getMessage(), 1000));
            delivery.setNextAttemptAt(attemptCount >= maxAttempts ? null : Instant.now().plusMillis(retryDelayMs));
            LOGGER.warn("Failed to send webhook to N8N for event {}: {}", delivery.getEventType(), ex.getMessage());
        }

        webhookDeliveryRepository.save(delivery);
    }

    private Object payloadNode(WebhookDelivery delivery) {
        try {
            JsonNode jsonNode = objectMapper.readTree(delivery.getPayloadJson());
            return jsonNode.isMissingNode() ? delivery.getPayloadJson() : jsonNode;
        } catch (Exception ex) {
            return delivery.getPayloadJson();
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
