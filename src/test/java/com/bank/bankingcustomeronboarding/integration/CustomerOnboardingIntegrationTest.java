package com.bank.bankingcustomeronboarding.integration;

import com.bank.bankingcustomeronboarding.BankingCustomerOnboardingApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = BankingCustomerOnboardingApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CustomerOnboardingIntegrationTest {

    private static final WireMockServer CKYC_SERVER =
            new WireMockServer(options().dynamicPort());

    static {
        // Start before Spring creates CKYCClient and resolves the dynamic property.
        CKYC_SERVER.start();
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @LocalServerPort
    private int applicationPort;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerCkycBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("ckyc.base-url", CKYC_SERVER::baseUrl);
    }

    @BeforeEach
    void resetWireMock() {
        CKYC_SERVER.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        CKYC_SERVER.stop();
    }

    @Test
    void verifiedCustomerIsOnboardedSuccessfully() throws Exception {
        stubCustomer("CUST101", 200, """
                {
                  "customerId": "CUST101",
                  "kycStatus": "VERIFIED",
                  "ckycNumber": "CKYC123456789",
                  "remarks": "KYC verification successful"
                }
                """);

        HttpResponse<String> response = onboard("CUST101");

        assertEquals(200, response.statusCode());
        assertEquals("SUCCESS", json(response).path("onboardingStatus").asText());
        CKYC_SERVER.verify(1, getRequestedFor(urlEqualTo("/ckyc/verify/CUST101")));
    }

    @Test
    void pendingCustomerReturnsPendingOnboardingStatus() throws Exception {
        stubCustomer("CUST102", 200, """
                {
                  "customerId": "CUST102",
                  "kycStatus": "PENDING",
                  "ckycNumber": null,
                  "remarks": "Documents are under verification"
                }
                """);

        HttpResponse<String> response = onboard("CUST102");

        assertEquals(200, response.statusCode());
        assertEquals("PENDING", json(response).path("onboardingStatus").asText());
    }

    @Test
    void rejectedCustomerReturnsRejectedOnboardingStatus() throws Exception {
        stubCustomer("CUST103", 200, """
                {
                  "customerId": "CUST103",
                  "kycStatus": "REJECTED",
                  "ckycNumber": null,
                  "remarks": "Submitted documents failed verification"
                }
                """);

        HttpResponse<String> response = onboard("CUST103");

        assertEquals(200, response.statusCode());
        assertEquals("REJECTED", json(response).path("onboardingStatus").asText());
    }

    @Test
    void missingCustomerReturnsStandardizedNotFoundError() throws Exception {
        stubCustomer("CUST104", 404, """
                {"error":"CUSTOMER_NOT_FOUND","message":"Customer does not exist in CKYC Registry"}
                """);

        HttpResponse<String> response = onboard("CUST104");

        assertEquals(404, response.statusCode());
        assertError(response, 404, "CKYC_CUSTOMER_NOT_FOUND");
    }

    @Test
    void unavailableRegistryReturnsStandardizedServiceUnavailableError() throws Exception {
        stubCustomer("CUST105", 503, """
                {"error":"SERVICE_UNAVAILABLE","message":"CKYC Registry is temporarily unavailable"}
                """);

        HttpResponse<String> response = onboard("CUST105");

        assertEquals(503, response.statusCode());
        assertError(response, 503, "CKYC_SERVICE_UNAVAILABLE");
    }

    @Test
    void delayedRegistryResponseReturnsStandardizedTimeoutError() throws Exception {
        CKYC_SERVER.stubFor(get(urlEqualTo("/ckyc/verify/CUST106"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(5_000)
                        .withBody("""
                                {"customerId":"CUST106","kycStatus":"VERIFIED"}
                                """)));

        HttpResponse<String> response = onboard("CUST106");

        assertEquals(504, response.statusCode());
        assertError(response, 504, "CKYC_TIMEOUT");
    }

    @Test
    void malformedRegistryResponseReturnsStandardizedBadGatewayError() throws Exception {
        stubCustomer("CUST107", 200, """
                {
                  "customerId": "CUST107",
                  "kycStatus": "VERIFIED",
                  "ckycNumber":
                }
                """);

        HttpResponse<String> response = onboard("CUST107");

        assertEquals(502, response.statusCode());
        assertError(response, 502, "CKYC_INVALID_RESPONSE");
    }

    private void stubCustomer(String customerId, int status, String responseBody) {
        CKYC_SERVER.stubFor(get(urlEqualTo("/ckyc/verify/" + customerId))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    private HttpResponse<String> onboard(String customerId) throws Exception {
        String requestBody = """
                {
                  "customerId": "%s",
                  "customerName": "Demo Customer",
                  "panNumber": "ABCDE1234F",
                  "dateOfBirth": "1990-01-01"
                }
                """.formatted(customerId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + applicationPort + "/api/customers/onboard"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) throws Exception {
        return objectMapper.readTree(response.body());
    }

    private void assertError(HttpResponse<String> response, int status, String error) throws Exception {
        JsonNode body = json(response);
        assertEquals(status, body.path("status").asInt());
        assertEquals(error, body.path("error").asText());
        assertTrue(body.path("message").isTextual());
    }
}
