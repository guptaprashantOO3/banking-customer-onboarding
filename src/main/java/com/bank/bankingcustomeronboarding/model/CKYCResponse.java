package com.bank.bankingcustomeronboarding.model;

public class CKYCResponse {

    private String customerId;
    private String kycStatus;
    private String ckycNumber;
    private String remarks;

    public CKYCResponse() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getCkycNumber() {
        return ckycNumber;
    }

    public void setCkycNumber(String ckycNumber) {
        this.ckycNumber = ckycNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}