package com.synkra.crm.controller;

import com.synkra.crm.dto.*;
import com.synkra.crm.service.MarketingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketingApiController {

    private final MarketingService marketingService;

    public MarketingApiController(MarketingService marketingService) {
        this.marketingService = marketingService;
    }

    @GetMapping("/email-providers")
    public List<EmailProviderResponse> listEmailProviders() {
        return marketingService.listEmailProviders();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/email-providers")
    public EmailProviderResponse createEmailProvider(@Valid @RequestBody CreateEmailProviderRequest request) {
        return marketingService.createEmailProvider(request);
    }

    @PostMapping("/email-providers/{providerId}/test")
    public void sendProviderTest(@PathVariable Long providerId,
                                 @Valid @RequestBody TestEmailProviderRequest request) {
        marketingService.sendProviderTest(providerId, request);
    }

    @GetMapping("/segments")
    public List<SegmentResponse> listSegments() {
        return marketingService.listSegments();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/segments")
    public SegmentResponse createSegment(@Valid @RequestBody CreateSegmentRequest request) {
        return marketingService.createSegment(request);
    }

    @PutMapping("/segments/{segmentId}/contacts")
    public SegmentResponse updateSegmentContacts(@PathVariable Long segmentId,
                                                 @Valid @RequestBody UpdateSegmentContactsRequest request) {
        return marketingService.updateSegmentContacts(segmentId, request);
    }

    @GetMapping("/campaigns")
    public List<EmailCampaignResponse> listCampaigns() {
        return marketingService.listCampaigns();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/campaigns")
    public EmailCampaignResponse createCampaign(@Valid @RequestBody CreateEmailCampaignRequest request) {
        return marketingService.createCampaign(request);
    }

    @PostMapping("/campaigns/{campaignId}/dispatch")
    public DispatchCampaignResponse dispatchCampaign(@PathVariable Long campaignId) {
        return marketingService.dispatchCampaign(campaignId);
    }

    @GetMapping("/forms")
    public List<FormDefinitionResponse> listForms() {
        return marketingService.listForms();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/forms")
    public FormDefinitionResponse createForm(@Valid @RequestBody CreateFormRequest request) {
        return marketingService.createForm(request);
    }
}
