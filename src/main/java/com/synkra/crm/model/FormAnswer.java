package com.synkra.crm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "form_answers")
public class FormAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private FormResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestion question;

    @Column(nullable = false)
    private String answerValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FormResponse getResponse() {
        return response;
    }

    public void setResponse(FormResponse response) {
        this.response = response;
    }

    public FormQuestion getQuestion() {
        return question;
    }

    public void setQuestion(FormQuestion question) {
        this.question = question;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }
}
