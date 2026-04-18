package com.synkra.crm.controller;

import com.synkra.crm.dto.PublicFormResponse;
import com.synkra.crm.dto.SubmitFormResponseRequest;
import com.synkra.crm.dto.SubmitFormResponseResult;
import com.synkra.crm.service.MarketingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/forms")
public class PublicFormsController {

    private final MarketingService marketingService;

    public PublicFormsController(MarketingService marketingService) {
        this.marketingService = marketingService;
    }

    @GetMapping("/{slug}")
    public PublicFormResponse getForm(@PathVariable String slug) {
        return marketingService.getPublicForm(slug);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{slug}/responses")
    public SubmitFormResponseResult submit(@PathVariable String slug,
                                           @Valid @RequestBody SubmitFormResponseRequest request) {
        return marketingService.submitPublicForm(slug, request);
    }
}
