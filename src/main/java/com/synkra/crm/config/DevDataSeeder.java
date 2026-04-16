package com.synkra.crm.config;

import com.synkra.crm.model.Activity;
import com.synkra.crm.model.ActivityType;
import com.synkra.crm.model.Contact;
import com.synkra.crm.model.ContactStatus;
import com.synkra.crm.model.Deal;
import com.synkra.crm.model.DealStage;
import com.synkra.crm.repository.ActivityRepository;
import com.synkra.crm.repository.ContactRepository;
import com.synkra.crm.repository.DealRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed-demo-data", name = "enabled", havingValue = "true")
public class DevDataSeeder implements ApplicationRunner {

    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;

    public DevDataSeeder(ContactRepository contactRepository,
                         DealRepository dealRepository,
                         ActivityRepository activityRepository) {
        this.contactRepository = contactRepository;
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (contactRepository.count() > 0 || dealRepository.count() > 0 || activityRepository.count() > 0) {
            return;
        }

        Instant now = Instant.now();

        Contact marina = buildContact("Marina Costa", "marina.costa@orbita.com", "11987650001", "Orbita Tech", ContactStatus.LEAD, now.minus(6, ChronoUnit.DAYS));
        Contact diego = buildContact("Diego Martins", "diego.martins@atlas.com", "11987650002", "Atlas Log", ContactStatus.QUALIFIED, now.minus(5, ChronoUnit.DAYS));
        Contact leticia = buildContact("Leticia Moraes", "leticia.moraes@neon.com", "11987650003", "Neon Health", ContactStatus.CUSTOMER, now.minus(4, ChronoUnit.DAYS));
        Contact renato = buildContact("Renato Lima", "renato.lima@vertice.com", "11987650004", "Vertice Consult", ContactStatus.QUALIFIED, now.minus(3, ChronoUnit.DAYS));
        Contact camila = buildContact("Camila Souza", "camila.souza@pulse.com", "11987650005", "Pulse Commerce", ContactStatus.LEAD, now.minus(1, ChronoUnit.DAYS));

        List<Contact> contacts = contactRepository.saveAll(List.of(marina, diego, leticia, renato, camila));

        dealRepository.saveAll(List.of(
            buildDeal("Expansao Orbita 2026", new BigDecimal("32000.00"), DealStage.PROSPECTING, LocalDate.now().plusDays(15), contacts.get(0), now.minus(5, ChronoUnit.DAYS)),
            buildDeal("Plano Atlas Enterprise", new BigDecimal("54000.00"), DealStage.PROPOSAL, LocalDate.now().plusDays(10), contacts.get(1), now.minus(4, ChronoUnit.DAYS)),
            buildDeal("Renovacao Neon Health", new BigDecimal("87000.00"), DealStage.WON, LocalDate.now().minusDays(1), contacts.get(2), now.minus(2, ChronoUnit.DAYS)),
            buildDeal("Projeto Vertice Premium", new BigDecimal("41000.00"), DealStage.NEGOTIATION, LocalDate.now().plusDays(7), contacts.get(3), now.minus(1, ChronoUnit.DAYS))
        ));

        activityRepository.saveAll(List.of(
            buildActivity(ActivityType.CALL, "Alinhar escopo da proposta comercial com Marina.", now.plus(1, ChronoUnit.DAYS), false, contacts.get(0), now.minus(5, ChronoUnit.DAYS)),
            buildActivity(ActivityType.EMAIL, "Enviar follow-up da proposta enterprise para Diego.", now.plus(6, ChronoUnit.HOURS), false, contacts.get(1), now.minus(4, ChronoUnit.DAYS)),
            buildActivity(ActivityType.MEETING, "Reuniao de onboarding com Leticia.", now.minus(1, ChronoUnit.DAYS), true, contacts.get(2), now.minus(2, ChronoUnit.DAYS)),
            buildActivity(ActivityType.TASK, "Atualizar previsao de fechamento do projeto Vertice.", now.plus(2, ChronoUnit.DAYS), false, contacts.get(3), now.minus(1, ChronoUnit.DAYS)),
            buildActivity(ActivityType.CALL, "Primeiro contato com Camila para qualificacao.", now.plus(4, ChronoUnit.HOURS), false, contacts.get(4), now)
        ));
    }

    private Contact buildContact(String name, String email, String phone, String company, ContactStatus status, Instant createdAt) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setCompany(company);
        contact.setStatus(status);
        contact.setCreatedAt(createdAt);
        return contact;
    }

    private Deal buildDeal(String title, BigDecimal value, DealStage stage, LocalDate expectedCloseDate, Contact contact, Instant createdAt) {
        Deal deal = new Deal();
        deal.setTitle(title);
        deal.setValue(value);
        deal.setStage(stage);
        deal.setExpectedCloseDate(expectedCloseDate);
        deal.setContact(contact);
        deal.setCreatedAt(createdAt);
        return deal;
    }

    private Activity buildActivity(ActivityType type, String notes, Instant dueAt, boolean completed, Contact contact, Instant createdAt) {
        Activity activity = new Activity();
        activity.setType(type);
        activity.setNotes(notes);
        activity.setDueAt(dueAt);
        activity.setCompleted(completed);
        activity.setContact(contact);
        activity.setCreatedAt(createdAt);
        return activity;
    }
}
