package com.bank.bankingcustomeronboarding.model;

import jakarta.validation.constraints.NotBlank;

public class CustomerRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "PAN number is required")
    private String panNumber;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    public CustomerRequest() {
    }

    public CustomerRequest(String customerId, String customerName,
                           String panNumber, String dateOfBirth) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.panNumber = panNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}