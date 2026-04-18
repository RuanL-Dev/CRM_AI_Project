package com.synkra.crm.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form_responses")
public class FormResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private FormDefinition form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    private String respondentName;

    private String respondentEmail;

    private String respondentPhone;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormAnswer> answers = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FormDefinition getForm() {
        return form;
    }

    public void setForm(FormDefinition form) {
        this.form = form;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public String getRespondentEmail() {
        return respondentEmail;
    }

    public void setRespondentEmail(String respondentEmail) {
        this.respondentEmail = respondentEmail;
    }

    public String getRespondentPhone() {
        return respondentPhone;
    }

    public void setRespondentPhone(String respondentPhone) {
        this.respondentPhone = respondentPhone;
    }

    public List<FormAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<FormAnswer> answers) {
        this.answers = answers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
