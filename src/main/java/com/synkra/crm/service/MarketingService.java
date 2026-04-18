package com.synkra.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkra.crm.dto.*;
import com.synkra.crm.model.*;
import com.synkra.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class MarketingService {

    private final ContactRepository contactRepository;
    private final EmailProviderRepository emailProviderRepository;
    private final SegmentRepository segmentRepository;
    private final EmailCampaignRepository emailCampaignRepository;
    private final CampaignDeliveryRepository campaignDeliveryRepository;
    private final FormDefinitionRepository formDefinitionRepository;
    private final FormResponseRepository formResponseRepository;
    private final EmailDeliveryService emailDeliveryService;
    private final N8nWebhookService n8nWebhookService;
    private final ObjectMapper objectMapper;

    public MarketingService(ContactRepository contactRepository,
                            EmailProviderRepository emailProviderRepository,
                            SegmentRepository segmentRepository,
                            EmailCampaignRepository emailCampaignRepository,
                            CampaignDeliveryRepository campaignDeliveryRepository,
                            FormDefinitionRepository formDefinitionRepository,
                            FormResponseRepository formResponseRepository,
                            EmailDeliveryService emailDeliveryService,
                            N8nWebhookService n8nWebhookService,
                            ObjectMapper objectMapper) {
        this.contactRepository = contactRepository;
        this.emailProviderRepository = emailProviderRepository;
        this.segmentRepository = segmentRepository;
        this.emailCampaignRepository = emailCampaignRepository;
        this.campaignDeliveryRepository = campaignDeliveryRepository;
        this.formDefinitionRepository = formDefinitionRepository;
        this.formResponseRepository = formResponseRepository;
        this.emailDeliveryService = emailDeliveryService;
        this.n8nWebhookService = n8nWebhookService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<EmailProviderResponse> listEmailProviders() {
        return emailProviderRepository.findAll().stream()
            .map(EmailProviderResponse::from)
            .toList();
    }

    @Transactional
    public EmailProviderResponse createEmailProvider(CreateEmailProviderRequest request) {
        EmailProvider provider = new EmailProvider();
        provider.setName(request.name());
        provider.setProviderType(request.providerType());
        provider.setHost(request.host());
        provider.setPort(request.port());
        provider.setUsername(request.username());
        provider.setPassword(request.password());
        provider.setFromEmail(request.fromEmail());
        provider.setFromName(request.fromName());
        provider.setReplyTo(request.replyTo());
        provider.setTlsEnabled(request.tlsEnabled());
        provider.setActive(request.active());

        return EmailProviderResponse.from(emailProviderRepository.save(provider));
    }

    @Transactional
    public void sendProviderTest(Long providerId, TestEmailProviderRequest request) {
        EmailProvider provider = emailProviderRepository.findById(providerId)
            .orElseThrow(() -> new NoSuchElementException("Provedor nao encontrado: " + providerId));
        emailDeliveryService.sendTest(provider, request.recipientEmail());
    }

    @Transactional(readOnly = true)
    public List<SegmentResponse> listSegments() {
        return segmentRepository.findAll().stream()
            .map(SegmentResponse::from)
            .sorted(Comparator.comparing(SegmentResponse::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Transactional
    public SegmentResponse createSegment(CreateSegmentRequest request) {
        segmentRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
            throw new IllegalArgumentException("Ja existe um segmento com esse nome.");
        });

        Segment segment = new Segment();
        segment.setName(request.name());
        segment.setDescription(request.description());
        segment.setColor(request.color());
        segment.setContacts(resolveContacts(request.contactIds()));
        return SegmentResponse.from(segmentRepository.save(segment));
    }

    @Transactional
    public SegmentResponse updateSegmentContacts(Long segmentId, UpdateSegmentContactsRequest request) {
        Segment segment = segmentRepository.findById(segmentId)
            .orElseThrow(() -> new NoSuchElementException("Segmento nao encontrado: " + segmentId));
        segment.setContacts(resolveContacts(request.contactIds()));
        return SegmentResponse.from(segmentRepository.save(segment));
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignResponse> listCampaigns() {
        return emailCampaignRepository.findAll().stream()
            .map(campaign -> EmailCampaignResponse.from(campaign, deliveriesOf(campaign.getId())))
            .toList();
    }

    @Transactional
    public EmailCampaignResponse createCampaign(CreateEmailCampaignRequest request) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setName(request.name());
        campaign.setSubject(request.subject());
        campaign.setPreviewText(request.previewText());
        campaign.setSenderName(request.senderName());
        campaign.setHtmlContent(request.htmlContent());
        campaign.setPlainTextContent(request.plainTextContent());
        campaign.setStatus(request.status() == null ? EmailCampaignStatus.DRAFT : request.status());
        campaign.setSegments(resolveSegments(request.segmentIds()));
        if (request.providerId() != null) {
            campaign.setProvider(emailProviderRepository.findById(request.providerId())
                .orElseThrow(() -> new NoSuchElementException("Provedor nao encontrado: " + request.providerId())));
        }

        EmailCampaign saved = emailCampaignRepository.save(campaign);
        return EmailCampaignResponse.from(saved, List.of());
    }

    @Transactional
    public DispatchCampaignResponse dispatchCampaign(Long campaignId) {
        EmailCampaign campaign = emailCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new NoSuchElementException("Campanha nao encontrada: " + campaignId));

        if (campaign.getProvider() == null) {
            throw new IllegalStateException("Selecione um provedor de email antes de disparar.");
        }
        if (!campaign.getProvider().isActive()) {
            throw new IllegalStateException("O provedor selecionado esta inativo.");
        }
        if (campaign.getSegments().isEmpty()) {
            throw new IllegalStateException("Associe ao menos um segmento a campanha.");
        }

        List<Contact> recipients = segmentRepository.findDistinctContactsBySegmentIds(
            campaign.getSegments().stream().map(Segment::getId).toList()
        );

        campaign.setStatus(EmailCampaignStatus.SENDING);
        int sent = 0;
        int failed = 0;

        for (Contact recipient : recipients) {
            CampaignDelivery delivery = new CampaignDelivery();
            delivery.setCampaign(campaign);
            delivery.setContact(recipient);
            delivery.setStatus(CampaignDeliveryStatus.PENDING);

            try {
                emailDeliveryService.sendCampaign(campaign.getProvider(), campaign, recipient);
                delivery.setStatus(CampaignDeliveryStatus.SENT);
                delivery.setSentAt(Instant.now());
                sent++;
            } catch (IllegalStateException exception) {
                delivery.setStatus(CampaignDeliveryStatus.FAILED);
                delivery.setFailureReason(exception.getMessage());
                failed++;
            }

            campaignDeliveryRepository.save(delivery);
        }

        campaign.setStatus(EmailCampaignStatus.SENT);
        emailCampaignRepository.save(campaign);
        n8nWebhookService.publish("campaign.dispatched", Map.of(
            "campaignId", campaign.getId(),
            "recipients", recipients.size(),
            "sent", sent,
            "failed", failed
        ));

        return new DispatchCampaignResponse(campaignId, recipients.size(), sent, failed);
    }

    @Transactional(readOnly = true)
    public List<FormDefinitionResponse> listForms() {
        return formDefinitionRepository.findAll().stream()
            .map(this::toFormDefinitionResponse)
            .toList();
    }

    @Transactional
    public FormDefinitionResponse createForm(CreateFormRequest request) {
        FormDefinition form = new FormDefinition();
        form.setName(request.name());
        form.setSlug(request.slug());
        form.setHeadline(request.headline());
        form.setDescription(request.description());
        form.setSubmitLabel(request.submitLabel());
        form.setSuccessTitle(request.successTitle());
        form.setSuccessMessage(request.successMessage());
        form.setActive(request.active());
        if (request.targetSegmentId() != null) {
            form.setTargetSegment(segmentRepository.findById(request.targetSegmentId())
                .orElseThrow(() -> new NoSuchElementException("Segmento alvo nao encontrado: " + request.targetSegmentId())));
        }

        form.getQuestions().clear();
        for (FormQuestionInput input : request.questions()) {
            FormQuestion question = new FormQuestion();
            question.setForm(form);
            question.setFieldKey(input.fieldKey());
            question.setLabel(input.label());
            question.setDescription(input.description());
            question.setPlaceholder(input.placeholder());
            question.setQuestionType(input.questionType());
            question.setRequired(input.required());
            question.setPositionIndex(input.positionIndex());
            question.setOptionsJson(writeOptions(input.options()));
            form.getQuestions().add(question);
        }

        return toFormDefinitionResponse(formDefinitionRepository.save(form));
    }

    @Transactional(readOnly = true)
    public PublicFormResponse getPublicForm(String slug) {
        FormDefinition form = formDefinitionRepository.findBySlug(slug)
            .filter(FormDefinition::isActive)
            .orElseThrow(() -> new NoSuchElementException("Formulario nao encontrado: " + slug));

        List<PublicFormQuestionResponse> questions = form.getQuestions().stream()
            .map(question -> new PublicFormQuestionResponse(
                question.getId(),
                question.getFieldKey(),
                question.getLabel(),
                question.getDescription(),
                question.getPlaceholder(),
                question.getQuestionType(),
                question.isRequired(),
                question.getPositionIndex(),
                readOptions(question.getOptionsJson())
            ))
            .toList();

        return new PublicFormResponse(
            form.getName(),
            form.getSlug(),
            form.getHeadline(),
            form.getDescription(),
            form.getSubmitLabel(),
            form.getSuccessTitle(),
            form.getSuccessMessage(),
            questions
        );
    }

    @Transactional
    public SubmitFormResponseResult submitPublicForm(String slug, SubmitFormResponseRequest request) {
        FormDefinition form = formDefinitionRepository.findBySlug(slug)
            .filter(FormDefinition::isActive)
            .orElseThrow(() -> new NoSuchElementException("Formulario nao encontrado: " + slug));

        Map<Long, FormQuestion> questionsById = new LinkedHashMap<>();
        for (FormQuestion question : form.getQuestions()) {
            questionsById.put(question.getId(), question);
        }

        FormResponse response = new FormResponse();
        response.setForm(form);

        String respondentName = null;
        String respondentEmail = null;
        String respondentPhone = null;

        for (FormAnswerInput answerInput : request.answers()) {
            FormQuestion question = questionsById.get(answerInput.questionId());
            if (question == null) {
                throw new IllegalArgumentException("Pergunta nao pertence ao formulario.");
            }
            if (question.isRequired() && answerInput.value().isBlank()) {
                throw new IllegalArgumentException("Preencha a resposta obrigatoria: " + question.getLabel());
            }

            if (question.getQuestionType() == FormQuestionType.EMAIL) {
                respondentEmail = answerInput.value().trim();
            }
            if (question.getQuestionType() == FormQuestionType.PHONE) {
                respondentPhone = answerInput.value().trim();
            }
            if ("name".equalsIgnoreCase(question.getFieldKey()) || "nome".equalsIgnoreCase(question.getFieldKey())) {
                respondentName = answerInput.value().trim();
            }

            FormAnswer answer = new FormAnswer();
            answer.setResponse(response);
            answer.setQuestion(question);
            answer.setAnswerValue(answerInput.value().trim());
            response.getAnswers().add(answer);
        }

        response.setRespondentName(respondentName);
        response.setRespondentEmail(respondentEmail);
        response.setRespondentPhone(respondentPhone);

        Contact contact = resolveOrCreateContact(respondentName, respondentEmail, respondentPhone);
        response.setContact(contact);

        if (contact != null && form.getTargetSegment() != null) {
            form.getTargetSegment().getContacts().add(contact);
        }

        FormResponse saved = formResponseRepository.save(response);
        n8nWebhookService.publish("form.response.created", Map.of(
            "formId", form.getId(),
            "slug", form.getSlug(),
            "responseId", saved.getId(),
            "contactId", contact == null ? null : contact.getId()
        ));

        return new SubmitFormResponseResult(
            saved.getId(),
            contact == null ? null : contact.getId(),
            form.getTargetSegment() == null ? null : form.getTargetSegment().getId()
        );
    }

    private Set<Contact> resolveContacts(List<Long> contactIds) {
        if (contactIds == null || contactIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(contactRepository.findAllById(contactIds));
    }

    private Set<Segment> resolveSegments(List<Long> segmentIds) {
        if (segmentIds == null || segmentIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(segmentRepository.findAllById(segmentIds));
    }

    private List<CampaignDeliveryResponse> deliveriesOf(Long campaignId) {
        return campaignDeliveryRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
            .map(CampaignDeliveryResponse::from)
            .toList();
    }

    private FormDefinitionResponse toFormDefinitionResponse(FormDefinition form) {
        List<FormQuestionResponse> questions = form.getQuestions().stream()
            .map(question -> FormQuestionResponse.from(question, readOptions(question.getOptionsJson())))
            .toList();
        List<FormResponseSummary> responses = formResponseRepository.findAll().stream()
            .filter(response -> response.getForm().getId().equals(form.getId()))
            .map(FormResponseSummary::from)
            .sorted(Comparator.comparing(FormResponseSummary::createdAt).reversed())
            .toList();
        return FormDefinitionResponse.from(form, questions, responses);
    }

    private String writeOptions(List<String> options) {
        try {
            return options == null || options.isEmpty() ? null : objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Nao foi possivel serializar as opcoes da pergunta.", exception);
        }
    }

    private List<String> readOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel ler as opcoes da pergunta.", exception);
        }
    }

    private Contact resolveOrCreateContact(String respondentName, String respondentEmail, String respondentPhone) {
        if (respondentEmail == null || respondentEmail.isBlank()) {
            return null;
        }

        Optional<Contact> existing = contactRepository.findByEmailIgnoreCase(respondentEmail);

        if (existing.isPresent()) {
            Contact contact = existing.get();
            if ((contact.getName() == null || contact.getName().isBlank()) && respondentName != null && !respondentName.isBlank()) {
                contact.setName(respondentName);
            }
            if ((contact.getPhone() == null || contact.getPhone().isBlank()) && respondentPhone != null && !respondentPhone.isBlank()) {
                contact.setPhone(respondentPhone);
            }
            return contact;
        }

        Contact contact = new Contact();
        contact.setName((respondentName == null || respondentName.isBlank()) ? respondentEmail : respondentName);
        contact.setEmail(respondentEmail);
        contact.setPhone(respondentPhone);
        contact.setStatus(ContactStatus.LEAD);
        return contactRepository.save(contact);
    }
}
