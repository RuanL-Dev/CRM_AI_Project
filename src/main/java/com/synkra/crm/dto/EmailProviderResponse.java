package com.synkra.crm.dto;

import com.synkra.crm.model.EmailProvider;
import com.synkra.crm.model.EmailProviderType;

import java.time.Instant;

public record EmailProviderResponse(
    Long id,
    String name,
    EmailProviderType providerType,
    String host,
    Integer port,
    String username,
    String fromEmail,
    String fromName,
    String replyTo,
    boolean tlsEnabled,
    boolean active,
    Instant createdAt
) {

    public static EmailProviderResponse from(EmailProvider provider) {
        return new EmailProviderResponse(
            provider.getId(),
            provider.getName(),
            provider.getProviderType(),
            provider.getHost(),
            provider.getPort(),
            provider.getUsername(),
            provider.getFromEmail(),
            provider.getFromName(),
            provider.getReplyTo(),
            provider.isTlsEnabled(),
            provider.isActive(),
            provider.getCreatedAt()
        );
    }
}
