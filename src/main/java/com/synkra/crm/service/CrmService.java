package com.synkra.crm.service;

import com.synkra.crm.dto.ActivityResponse;
import com.synkra.crm.dto.ContactResponse;
import com.synkra.crm.dto.CreateActivityRequest;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.dto.DashboardMetricsResponse;
import com.synkra.crm.dto.DashboardTimelineItemResponse;
import com.synkra.crm.dto.DealResponse;
import com.synkra.crm.model.*;
import com.synkra.crm.repository.ActivityTypeCountView;
import com.synkra.crm.repository.ActivityRepository;
import com.synkra.crm.repository.ContactRepository;
import com.synkra.crm.repository.ContactStatusCountView;
import com.synkra.crm.repository.DealRepository;
import com.synkra.crm.repository.DealStageValueView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public List<ContactResponse> listContacts() {
        return contactRepository.findAll().stream()
            .map(ContactResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DealResponse> listDeals() {
        return dealRepository.findAll().stream()
            .map(DealResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> listActivities() {
        return activityRepository.findAll().stream()
            .map(ActivityResponse::from)
            .toList();
    }

    @Transactional
    public ContactResponse createContact(CreateContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setCompany(request.company());
        contact.setStatus(request.status() == null ? ContactStatus.LEAD : request.status());

        Contact saved = contactRepository.save(contact);
        publishAfterCommit("contact.created", saved);
        return ContactResponse.from(saved);
    }

    @Transactional
    public DealResponse createDeal(CreateDealRequest request) {
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
        return DealResponse.from(saved);
    }

    @Transactional
    public DealResponse updateDealStage(Long dealId, DealStage stage) {
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new NoSuchElementException("Oportunidade não encontrada: " + dealId));

        deal.setStage(stage);
        Deal saved = dealRepository.save(deal);
        publishAfterCommit("deal.stage.updated", saved);
        return DealResponse.from(saved);
    }

    @Transactional
    public ActivityResponse createActivity(CreateActivityRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
            .orElseThrow(() -> new NoSuchElementException("Contato não encontrado: " + request.contactId()));

        Activity activity = new Activity();
        activity.setType(request.type());
        activity.setNotes(request.notes());
        activity.setDueAt(request.dueAt());
        activity.setContact(contact);

        Activity saved = activityRepository.save(activity);
        publishAfterCommit("activity.created", saved);
        return ActivityResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse dashboardMetrics() {
        long contactCount = contactRepository.count();
        long dealCount = dealRepository.count();
        long activityCount = activityRepository.count();
        long totalRecords = contactCount + dealCount + activityCount;

        Map<DealStage, BigDecimal> pipelineByStage = new EnumMap<>(DealStage.class);
        for (DealStage stage : DealStage.values()) {
            pipelineByStage.put(stage, BigDecimal.ZERO);
        }
        for (DealStageValueView item : dealRepository.sumValueGroupedByStage()) {
            pipelineByStage.put(item.getStage(), item.getTotal());
        }

        Map<ContactStatus, Long> leadStatusCounts = new EnumMap<>(ContactStatus.class);
        for (ContactStatus status : ContactStatus.values()) {
            leadStatusCounts.put(status, 0L);
        }
        for (ContactStatusCountView item : contactRepository.countGroupedByStatus()) {
            leadStatusCounts.put(item.getStatus(), item.getTotal());
        }

        Map<ActivityType, Long> activityTypeCounts = new EnumMap<>(ActivityType.class);
        for (ActivityType type : ActivityType.values()) {
            activityTypeCounts.put(type, 0L);
        }
        for (ActivityTypeCountView item : activityRepository.countGroupedByType()) {
            activityTypeCounts.put(item.getType(), item.getTotal());
        }

        BigDecimal projectedRevenue = dealRepository.sumValueWhereStageNot(DealStage.LOST);
        BigDecimal closedRevenue = dealRepository.sumValueWhereStage(DealStage.WON);

        BigDecimal conversionRate = contactCount == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(leadStatusCounts.get(ContactStatus.CUSTOMER))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(contactCount), 1, RoundingMode.HALF_UP);

        Instant now = Instant.now();
        long overdueActivities = activityRepository.countByCompletedFalseAndDueAtBefore(now);
        long scheduledActivities = activityRepository.countByCompletedFalseAndDueAtGreaterThanEqual(now);

        List<DashboardTimelineItemResponse> recordsTimeline = buildTimeline();

        return new DashboardMetricsResponse(
            contactCount,
            dealCount,
            activityCount,
            totalRecords,
            overdueActivities,
            scheduledActivities,
            projectedRevenue,
            closedRevenue,
            conversionRate,
            pipelineByStage,
            leadStatusCounts,
            activityTypeCounts,
            recordsTimeline
        );
    }

    private List<DashboardTimelineItemResponse> buildTimeline() {
        LocalDate today = LocalDate.now(DASHBOARD_ZONE);
        LocalDate startDate = today.minusDays(6);
        Instant start = startDate.atStartOfDay(DASHBOARD_ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(DASHBOARD_ZONE).toInstant();

        Map<LocalDate, long[]> countsByDate = new LinkedHashMap<>();
        mergeDailyCounts(countsByDate, contactRepository.countCreatedBetweenGroupedByDate(start, end), 0);
        mergeDailyCounts(countsByDate, dealRepository.countCreatedBetweenGroupedByDate(start, end), 1);
        mergeDailyCounts(countsByDate, activityRepository.countCreatedBetweenGroupedByDate(start, end), 2);

        List<DashboardTimelineItemResponse> timeline = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long[] counters = countsByDate.getOrDefault(date, new long[3]);
            timeline.add(new DashboardTimelineItemResponse(
                date.toString(),
                date.getDayOfMonth() + "/" + String.format("%02d", date.getMonthValue()),
                counters[0],
                counters[1],
                counters[2],
                counters[0] + counters[1] + counters[2]
            ));
        }

        return timeline;
    }

    private void mergeDailyCounts(Map<LocalDate, long[]> buckets, List<Object[]> rows, int index) {
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            long total = ((Number) row[1]).longValue();
            long[] counters = buckets.computeIfAbsent(date, ignored -> new long[3]);
            counters[index] = total;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
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
