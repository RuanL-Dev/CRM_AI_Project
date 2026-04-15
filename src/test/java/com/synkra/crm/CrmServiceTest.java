package com.synkra.crm;

import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.model.Contact;
import com.synkra.crm.model.ContactStatus;
import com.synkra.crm.model.Deal;
import com.synkra.crm.model.DealStage;
import com.synkra.crm.repository.ActivityRepository;
import com.synkra.crm.repository.ContactRepository;
import com.synkra.crm.repository.DealRepository;
import com.synkra.crm.service.CrmService;
import com.synkra.crm.service.N8nWebhookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private N8nWebhookService n8nWebhookService;

    @InjectMocks
    private CrmService crmService;

    @Test
    void createContactAppliesDefaultStatusAndPublishesEvent() {
        CreateContactRequest request = new CreateContactRequest(
            "Ana Costa",
            "ana@example.com",
            "11999990000",
            "Synkra",
            null
        );

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(1L);
            return contact;
        });

        Contact saved = crmService.createContact(request);

        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(captor.capture());
        verify(n8nWebhookService, times(1)).publish("contact.created", saved);

        assertThat(captor.getValue().getStatus()).isEqualTo(ContactStatus.LEAD);
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void createDealUsesExistingContactAndPublishesEvent() {
        Contact contact = new Contact();
        contact.setId(7L);
        contact.setName("Ana Costa");
        contact.setEmail("ana@example.com");
        contact.setStatus(ContactStatus.LEAD);

        CreateDealRequest request = new CreateDealRequest(
            "Plano Enterprise",
            new BigDecimal("15000.00"),
            DealStage.PROPOSAL,
            LocalDate.of(2026, 5, 10),
            7L
        );

        when(contactRepository.findById(7L)).thenReturn(Optional.of(contact));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> {
            Deal deal = invocation.getArgument(0);
            deal.setId(11L);
            return deal;
        });

        Deal saved = crmService.createDeal(request);

        assertThat(saved.getId()).isEqualTo(11L);
        assertThat(saved.getContact()).isSameAs(contact);
        assertThat(saved.getStage()).isEqualTo(DealStage.PROPOSAL);
        verify(n8nWebhookService).publish("deal.created", saved);
    }
}
