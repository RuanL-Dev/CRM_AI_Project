package com.synkra.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkra.crm.dto.CreateActivityRequest;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.model.ActivityType;
import com.synkra.crm.model.ContactStatus;
import com.synkra.crm.model.DealStage;
import com.synkra.crm.repository.ActivityRepository;
import com.synkra.crm.repository.ContactRepository;
import com.synkra.crm.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmApiSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void clearData() {
        activityRepository.deleteAll();
        dealRepository.deleteAll();
        contactRepository.deleteAll();
    }

    @Test
    void contactsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/contacts"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginPageLoadsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/auth/login.html"));
    }

    @Test
    void dashboardLoadsForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password")))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/ui/index.html"));
    }

    @Test
    void createContactWorksForAuthenticatedUser() throws Exception {
        CreateContactRequest request = new CreateContactRequest(
            "Maria Silva",
            "maria@example.com",
            "11999999999",
            "Synkra",
            ContactStatus.LEAD
        );

        mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.email").value("maria@example.com"))
            .andExpect(jsonPath("$.status").value("LEAD"));
    }

    @Test
    void createContactReturnsValidationErrorForInvalidEmail() throws Exception {
        CreateContactRequest request = new CreateContactRequest(
            "Maria Silva",
            "invalid-email",
            null,
            null,
            ContactStatus.LEAD
        );

        mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("email: informe um e-mail válido"));
    }

    @Test
    void createContactReturnsConflictForDuplicatedEmail() throws Exception {
        CreateContactRequest request = new CreateContactRequest(
            "Maria Silva",
            "maria@example.com",
            "11999999999",
            "Synkra",
            ContactStatus.LEAD
        );

        mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Já existe um contato cadastrado com este e-mail"));
    }

    @Test
    void csrfEndpointReturnsTokenForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/security/csrf")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));
    }

    @Test
    void createDealWorksForAuthenticatedUser() throws Exception {
        CreateContactRequest contactRequest = new CreateContactRequest(
            "Paula Rocha",
            "paula@example.com",
            "11988887777",
            "Synkra",
            ContactStatus.QUALIFIED
        );

        String contactJson = mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long contactId = objectMapper.readTree(contactJson).get("id").asLong();

        CreateDealRequest dealRequest = new CreateDealRequest(
            "Projeto Premium",
            BigDecimal.valueOf(15000),
            DealStage.PROPOSAL,
            LocalDate.now().plusDays(7),
            contactId
        );

        mockMvc.perform(post("/api/deals")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dealRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("Projeto Premium"))
            .andExpect(jsonPath("$.contact.id").value(contactId));
    }

    @Test
    void createActivityWorksForAuthenticatedUser() throws Exception {
        CreateContactRequest contactRequest = new CreateContactRequest(
            "Ricardo Alves",
            "ricardo@example.com",
            "11988886666",
            "Synkra",
            ContactStatus.LEAD
        );

        String contactJson = mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long contactId = objectMapper.readTree(contactJson).get("id").asLong();

        CreateActivityRequest activityRequest = new CreateActivityRequest(
            ActivityType.CALL,
            "Retornar com proposta comercial",
            Instant.now().plusSeconds(3600),
            contactId
        );

        mockMvc.perform(post("/api/activities")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.type").value("CALL"))
            .andExpect(jsonPath("$.contact.id").value(contactId));
    }

    @Test
    void updateDealStageWorksForAuthenticatedUser() throws Exception {
        CreateContactRequest contactRequest = new CreateContactRequest(
            "Fernanda Lima",
            "fernanda@example.com",
            "11988885555",
            "Synkra",
            ContactStatus.LEAD
        );

        String contactJson = mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long contactId = objectMapper.readTree(contactJson).get("id").asLong();

        CreateDealRequest dealRequest = new CreateDealRequest(
            "Renovacao Enterprise",
            BigDecimal.valueOf(9000),
            DealStage.PROSPECTING,
            LocalDate.now().plusDays(14),
            contactId
        );

        String dealJson = mockMvc.perform(post("/api/deals")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dealRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long dealId = objectMapper.readTree(dealJson).get("id").asLong();

        mockMvc.perform(patch("/api/deals/{dealId}/stage", dealId)
                .param("stage", "WON")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dealId))
            .andExpect(jsonPath("$.stage").value("WON"));
    }

    @Test
    void metricsEndpointReturnsStructuredDashboardPayload() throws Exception {
        CreateContactRequest contactRequest = new CreateContactRequest(
            "Beatriz Rocha",
            "beatriz@example.com",
            "11988884444",
            "Synkra",
            ContactStatus.CUSTOMER
        );

        String contactJson = mockMvc.perform(post("/api/contacts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long contactId = objectMapper.readTree(contactJson).get("id").asLong();

        mockMvc.perform(post("/api/deals")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateDealRequest(
                    "Expansao",
                    BigDecimal.valueOf(12000),
                    DealStage.PROSPECTING,
                    LocalDate.now().plusDays(10),
                    contactId
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/activities")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateActivityRequest(
                    ActivityType.EMAIL,
                    "Enviar proposta",
                    Instant.now().plusSeconds(3600),
                    contactId
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/metrics")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contacts").value(1))
            .andExpect(jsonPath("$.deals").value(1))
            .andExpect(jsonPath("$.activities").value(1))
            .andExpect(jsonPath("$.leadStatusCounts.CUSTOMER").value(1))
            .andExpect(jsonPath("$.pipelineByStage.PROSPECTING").value(12000))
            .andExpect(jsonPath("$.recordsTimeline").isArray());
    }
}
