package com.synkra.crm.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forms")
public class FormDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String headline;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String submitLabel = "Continuar";

    @Column(nullable = false)
    private String successTitle = "Tudo certo";

    @Column(nullable = false, length = 1000)
    private String successMessage = "Recebemos suas respostas.";

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_segment_id")
    private Segment targetSegment;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    private List<FormQuestion> questions = new ArrayList<>();

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubmitLabel() {
        return submitLabel;
    }

    public void setSubmitLabel(String submitLabel) {
        this.submitLabel = submitLabel;
    }

    public String getSuccessTitle() {
        return successTitle;
    }

    public void setSuccessTitle(String successTitle) {
        this.successTitle = successTitle;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Segment getTargetSegment() {
        return targetSegment;
    }

    public void setTargetSegment(Segment targetSegment) {
        this.targetSegment = targetSegment;
    }

    public List<FormQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<FormQuestion> questions) {
        this.questions = questions;
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
