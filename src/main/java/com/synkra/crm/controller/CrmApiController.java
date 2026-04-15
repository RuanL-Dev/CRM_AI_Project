package com.synkra.crm.controller;

import com.synkra.crm.dto.CreateActivityRequest;
import com.synkra.crm.dto.CreateContactRequest;
import com.synkra.crm.dto.CreateDealRequest;
import com.synkra.crm.model.Activity;
import com.synkra.crm.model.Contact;
import com.synkra.crm.model.Deal;
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
    public List<Contact> contacts() {
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
    public Contact createContact(@Valid @RequestBody CreateContactRequest request) {
        return crmService.createContact(request);
    }

    @GetMapping("/deals")
    public List<Deal> deals() {
        return crmService.listDeals();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/deals")
    public Deal createDeal(@Valid @RequestBody CreateDealRequest request) {
        return crmService.createDeal(request);
    }

    @PatchMapping("/deals/{dealId}/stage")
    public Deal updateDealStage(@PathVariable Long dealId,
                                @RequestParam DealStage stage) {
        return crmService.updateDealStage(dealId, stage);
    }

    @GetMapping("/activities")
    public List<Activity> activities() {
        return crmService.listActivities();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/activities")
    public Activity createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return crmService.createActivity(request);
    }

    @GetMapping("/dashboard/metrics")
    public Map<String, Object> metrics() {
        return crmService.dashboardMetrics();
    }
}
