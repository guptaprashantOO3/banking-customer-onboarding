package com.bank.bankingcustomeronboarding.controller;

import com.bank.bankingcustomeronboarding.model.CustomerRequest;
import com.bank.bankingcustomeronboarding.model.OnboardingResponse;
import com.bank.bankingcustomeronboarding.service.CustomerOnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class CustomerOnboardingController {

    private final CustomerOnboardingService onboardingService;

    public CustomerOnboardingController(CustomerOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping
    public OnboardingResponse onboardCustomer(@Valid @RequestBody CustomerRequest request) {
        return onboardingService.onboardCustomer(request);
    }
}
