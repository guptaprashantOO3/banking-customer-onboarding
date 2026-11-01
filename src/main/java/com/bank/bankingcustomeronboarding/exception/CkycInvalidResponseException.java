package com.bank.bankingcustomeronboarding.exception;

public class CkycInvalidResponseException extends RuntimeException {

    public CkycInvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}