package com.synkra.crm.service;

import com.synkra.crm.dto.CreateActivityRequest;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.model.*;
import com.synkra.crm.repository.ActivityRepository;
import com.synkra.crm.repository.ContactRepository;
import com.synkra.crm.repository.DealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CrmService {

    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final N8nWebhookService n8nWebhookService;

    public CrmService(ContactRepository contactRepository,
                      DealRepository dealRepository,
                      ActivityRepository activityRepository,
                      N8nWebhookService n8nWebhookService) {
        this.contactRepository = contactRepository;
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.n8nWebhookService = n8nWebhookService;
    }

    public List<Contact> listContacts() {
        return contactRepository.findAll();
    }

    public List<Deal> listDeals() {
        return dealRepository.findAll();
    }

    public List<Activity> listActivities() {
        return activityRepository.findAll();
    }

    @Transactional
    public Contact createContact(CreateContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setCompany(request.company());
        contact.setStatus(request.status() == null ? ContactStatus.LEAD : request.status());

        Contact saved = contactRepository.save(contact);
        publishAfterCommit("contact.created", saved);
        return saved;
    }

    @Transactional
    public Deal createDeal(CreateDealRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
            .orElseThrow(() -> new NoSuchElementException("Contact not found: " + request.contactId()));

        Deal deal = new Deal();
        deal.setTitle(request.title());
        deal.setValue(request.value());
        deal.setStage(request.stage() == null ? DealStage.PROSPECTING : request.stage());
        deal.setExpectedCloseDate(request.expectedCloseDate());
        deal.setContact(contact);

        Deal saved = dealRepository.save(deal);
        publishAfterCommit("deal.created", saved);
        return saved;
    }

    @Transactional
    public Deal updateDealStage(Long dealId, DealStage stage) {
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new NoSuchElementException("Deal not found: " + dealId));

        deal.setStage(stage);
        Deal saved = dealRepository.save(deal);
        publishAfterCommit("deal.stage.updated", saved);
        return saved;
    }

    @Transactional
    public Activity createActivity(CreateActivityRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
            .orElseThrow(() -> new NoSuchElementException("Contact not found: " + request.contactId()));

        Activity activity = new Activity();
        activity.setType(request.type());
        activity.setNotes(request.notes());
        activity.setDueAt(request.dueAt());
        activity.setContact(contact);

        Activity saved = activityRepository.save(activity);
        publishAfterCommit("activity.created", saved);
        return saved;
    }

    public Map<String, Object> dashboardMetrics() {
        long contactCount = contactRepository.count();
        long dealCount = dealRepository.count();
        long activityCount = activityRepository.count();

        Map<DealStage, BigDecimal> pipelineByStage = new EnumMap<>(DealStage.class);
        for (DealStage stage : DealStage.values()) {
            pipelineByStage.put(stage, BigDecimal.ZERO);
        }
        for (Deal deal : dealRepository.findAll()) {
            pipelineByStage.compute(deal.getStage(), (k, v) -> v.add(deal.getValue()));
        }

        return Map.of(
            "contacts", contactCount,
            "deals", dealCount,
            "activities", activityCount,
            "pipelineByStage", pipelineByStage
        );
    }

    private void publishAfterCommit(String eventType, Object payload) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            n8nWebhookService.publish(eventType, payload);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                n8nWebhookService.publish(eventType, payload);
            }
        });
    }
}
