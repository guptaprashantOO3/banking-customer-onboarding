package com.bank.bankingcustomeronboarding.service;

import com.bank.bankingcustomeronboarding.client.CKYCClient;
import com.bank.bankingcustomeronboarding.model.CKYCResponse;
import com.bank.bankingcustomeronboarding.model.CustomerRequest;
import com.bank.bankingcustomeronboarding.model.OnboardingResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerOnboardingService {

    private final CKYCClient ckycClient;

    public CustomerOnboardingService(CKYCClient ckycClient) {
        this.ckycClient = ckycClient;
    }

    public OnboardingResponse onboardCustomer(CustomerRequest request) {

        // Step 1: Get the synthetic customer ID
        String customerId = request.getCustomerId();

        // Step 2: Call external CKYC service
        CKYCResponse ckycResponse =
                ckycClient.verifyCustomer(customerId);

        // Step 3: Check KYC status
        if ("VERIFIED".equalsIgnoreCase(ckycResponse.getKycStatus())) {

            return new OnboardingResponse(
                    customerId,
                    request.getCustomerName(),
                    "SUCCESS",
                    "Customer onboarded successfully"
            );
        }

        if ("PENDING".equalsIgnoreCase(ckycResponse.getKycStatus())) {

            return new OnboardingResponse(
                    customerId,
                    request.getCustomerName(),
                    "PENDING",
                    "KYC verification is pending"
            );
        }

        return new OnboardingResponse(
                customerId,
                request.getCustomerName(),
                "REJECTED",
                "KYC verification failed"
        );
    }
}