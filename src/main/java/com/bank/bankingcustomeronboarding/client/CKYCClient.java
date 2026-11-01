
package com.bank.bankingcustomeronboarding.client;

import com.bank.bankingcustomeronboarding.model.CKYCResponse;
import com.bank.bankingcustomeronboarding.exception.CkycInvalidResponseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class CKYCClient {

    private final RestClient restClient;

    public CKYCClient(
            @Value("${ckyc.base-url}") String baseUrl) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        // Connection timeout: 2 seconds
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));

        // Read timeout: 2 seconds
        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        // Create RestClient using configurable base URL
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public CKYCResponse verifyCustomer(String customerId) {

        try {
            return restClient.get()
                    .uri("/ckyc/verify/{customerId}", customerId)
                    .retrieve()
                    .body(CKYCResponse.class);

        } catch (RestClientException exception) {

            if (hasCause(exception, HttpMessageNotReadableException.class)) {

                throw new CkycInvalidResponseException(
                        "CKYC Registry returned malformed JSON",
                        exception
                );
            }

            throw exception;
        }
    }

    private boolean hasCause(
            Throwable exception,
            Class<? extends Throwable> causeType) {

        Throwable cause = exception;

        while (cause != null) {

            if (causeType.isInstance(cause)) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
