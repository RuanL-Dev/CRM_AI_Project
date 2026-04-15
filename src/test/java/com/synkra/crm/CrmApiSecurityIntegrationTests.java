package com.synkra.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.model.ContactStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            .andExpect(jsonPath("$.error").value("email: must be a well-formed email address"));
    }

    @Test
    void csrfEndpointReturnsTokenForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/security/csrf")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test-user", "test-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));
    }
}
