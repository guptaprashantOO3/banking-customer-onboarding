package com.bank.bankingcustomeronboarding.exception;

import com.bank.bankingcustomeronboarding.model.ErrorResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import java.net.SocketTimeoutException;
import com.bank.bankingcustomeronboarding.exception.CkycInvalidResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleHttpStatusException(
            HttpStatusCodeException exception) {

        int status = exception.getStatusCode().value();

        String error;
        String message;

        if (status == 404) {
            error = "CKYC_CUSTOMER_NOT_FOUND";
            message = "Customer not found in CKYC Registry";
        } else if (status == 503) {
            error = "CKYC_SERVICE_UNAVAILABLE";
            message = "CKYC Registry is temporarily unavailable";
        } else {
            error = "CKYC_SERVICE_ERROR";
            message = "CKYC Registry request failed";
        }

        ErrorResponse response =
                new ErrorResponse(status, error, message);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(
            ResourceAccessException exception) {

        Throwable cause = exception;

        while (cause != null &&
                !(cause instanceof SocketTimeoutException)) {
            cause = cause.getCause();
        }

        if (cause instanceof SocketTimeoutException) {

            ErrorResponse response = new ErrorResponse(
                    504,
                    "CKYC_TIMEOUT",
                    "CKYC Registry response timed out"
            );

            return ResponseEntity.status(504).body(response);
        }

        ErrorResponse response = new ErrorResponse(
                503,
                "CKYC_CONNECTION_ERROR",
                "Unable to communicate with CKYC Registry"
        );

        return ResponseEntity.status(503).body(response);
    }

    @ExceptionHandler(CkycInvalidResponseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCkycResponse(
            CkycInvalidResponseException exception) {

        ErrorResponse response = new ErrorResponse(
                502,
                "CKYC_INVALID_RESPONSE",
                "CKYC Registry returned an invalid response"
        );

        return ResponseEntity.status(502).body(response);
    }
}