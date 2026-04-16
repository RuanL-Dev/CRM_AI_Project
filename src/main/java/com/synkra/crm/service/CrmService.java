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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CrmService {
    private static final ZoneId DASHBOARD_ZONE = ZoneId.of("America/Sao_Paulo");


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
            .orElseThrow(() -> new NoSuchElementException("Contato não encontrado: " + request.contactId()));

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
            .orElseThrow(() -> new NoSuchElementException("Oportunidade não encontrada: " + dealId));

        deal.setStage(stage);
        Deal saved = dealRepository.save(deal);
        publishAfterCommit("deal.stage.updated", saved);
        return saved;
    }

    @Transactional
    public Activity createActivity(CreateActivityRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
            .orElseThrow(() -> new NoSuchElementException("Contato não encontrado: " + request.contactId()));

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
        List<Contact> contacts = contactRepository.findAll();
        List<Deal> deals = dealRepository.findAll();
        List<Activity> activities = activityRepository.findAll();

        long contactCount = contacts.size();
        long dealCount = deals.size();
        long activityCount = activities.size();
        long totalRecords = contactCount + dealCount + activityCount;

        Map<DealStage, BigDecimal> pipelineByStage = new EnumMap<>(DealStage.class);
        for (DealStage stage : DealStage.values()) {
            pipelineByStage.put(stage, BigDecimal.ZERO);
        }
        for (Deal deal : deals) {
            pipelineByStage.compute(deal.getStage(), (k, v) -> v.add(deal.getValue()));
        }

        Map<ContactStatus, Long> leadStatusCounts = new EnumMap<>(ContactStatus.class);
        for (ContactStatus status : ContactStatus.values()) {
            leadStatusCounts.put(status, 0L);
        }
        for (Contact contact : contacts) {
            leadStatusCounts.compute(contact.getStatus(), (k, v) -> v + 1L);
        }

        Map<ActivityType, Long> activityTypeCounts = new EnumMap<>(ActivityType.class);
        for (ActivityType type : ActivityType.values()) {
            activityTypeCounts.put(type, 0L);
        }
        for (Activity activity : activities) {
            activityTypeCounts.compute(activity.getType(), (k, v) -> v + 1L);
        }

        BigDecimal projectedRevenue = deals.stream()
            .filter(deal -> deal.getStage() != DealStage.LOST)
            .map(Deal::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal closedRevenue = deals.stream()
            .filter(deal -> deal.getStage() == DealStage.WON)
            .map(Deal::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal conversionRate = contactCount == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(leadStatusCounts.get(ContactStatus.CUSTOMER))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(contactCount), 1, RoundingMode.HALF_UP);

        long overdueActivities = activities.stream()
            .filter(activity -> !activity.isCompleted())
            .filter(activity -> activity.getDueAt() != null && activity.getDueAt().isBefore(Instant.now()))
            .count();

        long scheduledActivities = activities.stream()
            .filter(activity -> !activity.isCompleted())
            .filter(activity -> activity.getDueAt() != null && !activity.getDueAt().isBefore(Instant.now()))
            .count();

        List<Map<String, Object>> recordsTimeline = buildTimeline(contacts, deals, activities);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("contacts", contactCount);
        metrics.put("deals", dealCount);
        metrics.put("activities", activityCount);
        metrics.put("totalRecords", totalRecords);
        metrics.put("overdueActivities", overdueActivities);
        metrics.put("scheduledActivities", scheduledActivities);
        metrics.put("projectedRevenue", projectedRevenue);
        metrics.put("closedRevenue", closedRevenue);
        metrics.put("conversionRate", conversionRate);
        metrics.put("pipelineByStage", pipelineByStage);
        metrics.put("leadStatusCounts", leadStatusCounts);
        metrics.put("activityTypeCounts", activityTypeCounts);
        metrics.put("recordsTimeline", recordsTimeline);
        return metrics;
    }

    private List<Map<String, Object>> buildTimeline(List<Contact> contacts, List<Deal> deals, List<Activity> activities) {
        LocalDate today = LocalDate.now(DASHBOARD_ZONE);
        Map<LocalDate, Map<String, Object>> buckets = new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("label", date.getDayOfMonth() + "/" + String.format("%02d", date.getMonthValue()));
            item.put("leads", 0L);
            item.put("oportunidades", 0L);
            item.put("atividades", 0L);
            item.put("total", 0L);
            buckets.put(date, item);
        }

        contacts.forEach(contact -> incrementTimeline(buckets, contact.getCreatedAt(), "leads"));
        deals.forEach(deal -> incrementTimeline(buckets, deal.getCreatedAt(), "oportunidades"));
        activities.forEach(activity -> incrementTimeline(buckets, activity.getCreatedAt(), "atividades"));

        return new ArrayList<>(buckets.values());
    }

    private void incrementTimeline(Map<LocalDate, Map<String, Object>> buckets, Instant instant, String key) {
        if (instant == null) {
            return;
        }

        LocalDate date = instant.atZone(DASHBOARD_ZONE).toLocalDate();
        Map<String, Object> bucket = buckets.get(date);
        if (bucket == null) {
            return;
        }

        long currentValue = (long) bucket.get(key);
        long currentTotal = (long) bucket.get("total");
        bucket.put(key, currentValue + 1L);
        bucket.put("total", currentTotal + 1L);
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
