package com.synkra.crm.controller;

import com.synkra.crm.dto.CreateActivityRequest;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.dto.ActivityResponse;
import com.synkra.crm.dto.ContactResponse;
import com.synkra.crm.dto.DashboardMetricsResponse;
import com.synkra.crm.dto.DealResponse;
import com.synkra.crm.model.DealStage;
import com.synkra.crm.service.CrmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CrmApiController {

    private final CrmService crmService;

    public CrmApiController(CrmService crmService) {
        this.crmService = crmService;
    }

    @GetMapping("/contacts")
    public List<ContactResponse> contacts() {
        return crmService.listContacts();
    }

    @GetMapping("/security/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName()
        );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/contacts")
    public ContactResponse createContact(@Valid @RequestBody CreateContactRequest request) {
        return crmService.createContact(request);
    }

    @GetMapping("/deals")
    public List<DealResponse> deals() {
        return crmService.listDeals();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/deals")
    public DealResponse createDeal(@Valid @RequestBody CreateDealRequest request) {
        return crmService.createDeal(request);
    }

    @PatchMapping("/deals/{dealId}/stage")
    public DealResponse updateDealStage(@PathVariable Long dealId,
                                        @RequestParam DealStage stage) {
        return crmService.updateDealStage(dealId, stage);
    }

    @GetMapping("/activities")
    public List<ActivityResponse> activities() {
        return crmService.listActivities();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/activities")
    public ActivityResponse createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return crmService.createActivity(request);
    }

    @GetMapping("/dashboard/metrics")
    public DashboardMetricsResponse metrics() {
        return crmService.dashboardMetrics();
    }
}
