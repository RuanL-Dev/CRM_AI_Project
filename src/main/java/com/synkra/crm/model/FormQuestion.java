package com.synkra.crm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "form_questions")
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private FormDefinition form;

    @Column(nullable = false)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Column(length = 500)
    private String description;

    private String placeholder;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private FormQuestionType questionType = FormQuestionType.SHORT_TEXT;

    @Column(nullable = false)
    private boolean required = true;

    @Column(nullable = false)
    private Integer positionIndex;

    private String optionsJson;

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

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public FormQuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(FormQuestionType questionType) {
        this.questionType = questionType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(Integer positionIndex) {
        this.positionIndex = positionIndex;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }
}
