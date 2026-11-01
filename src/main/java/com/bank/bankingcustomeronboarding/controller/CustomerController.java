package com.bank.bankingcustomeronboarding.controller;

import com.bank.bankingcustomeronboarding.model.CustomerRequest;
import com.bank.bankingcustomeronboarding.model.OnboardingResponse;
import com.bank.bankingcustomeronboarding.service.CustomerOnboardingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerOnboardingService onboardingService;

    public CustomerController(CustomerOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<OnboardingResponse> onboardCustomer(
            @Valid @RequestBody CustomerRequest request) {

        OnboardingResponse response =
                onboardingService.onboardCustomer(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/onboard")
    public ResponseEntity<OnboardingResponse> demoOnboarding() {
        CustomerRequest demoRequest = new CustomerRequest(
                "CUST101",
                "Demo Customer",
                "ABCDE1234F",
                "1990-01-01"
        );
        return ResponseEntity.ok(onboardingService.onboardCustomer(demoRequest));
    }
}
