package com.synkra.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class N8nWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(N8nWebhookService.class);

    private final RestTemplate restTemplate;

    @Value("${n8n.webhook-url:}")
    private String webhookUrl;

    public N8nWebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void publish(String eventType, Object payload) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        Map<String, Object> envelope = Map.of(
            "eventType", eventType,
            "sentAt", Instant.now().toString(),
            "payload", payload
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(envelope, headers), Void.class);
        } catch (Exception ex) {
            LOGGER.warn("Failed to send webhook to N8N for event {}: {}", eventType, ex.getMessage());
        }
    }
}
