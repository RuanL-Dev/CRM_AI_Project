package com.synkra.crm.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "email_campaigns")
public class EmailCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String subject;

    private String previewText;

    private String senderName;

    @Column(nullable = false)
    private String htmlContent;

    private String plainTextContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailCampaignStatus status = EmailCampaignStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private EmailProvider provider;

    @ManyToMany
    @JoinTable(
        name = "campaign_segments",
        joinColumns = @JoinColumn(name = "campaign_id"),
        inverseJoinColumns = @JoinColumn(name = "segment_id")
    )
    private Set<Segment> segments = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getPlainTextContent() {
        return plainTextContent;
    }

    public void setPlainTextContent(String plainTextContent) {
        this.plainTextContent = plainTextContent;
    }

    public EmailCampaignStatus getStatus() {
        return status;
    }

    public void setStatus(EmailCampaignStatus status) {
        this.status = status;
    }

    public EmailProvider getProvider() {
        return provider;
    }

    public void setProvider(EmailProvider provider) {
        this.provider = provider;
    }

    public Set<Segment> getSegments() {
        return segments;
    }

    public void setSegments(Set<Segment> segments) {
        this.segments = segments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
