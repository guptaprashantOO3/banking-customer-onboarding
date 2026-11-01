package com.bank.bankingcustomeronboarding.model;

public class OnboardingResponse {

    private String customerId;
    private String customerName;
    private String onboardingStatus;
    private String message;

    public OnboardingResponse() {
    }

    public OnboardingResponse(String customerId,
                              String customerName,
                              String onboardingStatus,
                              String message) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.onboardingStatus = onboardingStatus;
        this.message = message;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOnboardingStatus() {
        return onboardingStatus;
    }

    public String getMessage() {
        return message;
    }
}