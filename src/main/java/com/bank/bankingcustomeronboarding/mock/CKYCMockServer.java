package com.bank.bankingcustomeronboarding.mock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


public class CKYCMockServer {

    public static void main(String[] args) {

        // Step 1: Create WireMock server on port 8081
        WireMockServer wireMockServer =
                new WireMockServer(options().port(8081));

        // Step 2: Start the server
        wireMockServer.start();

        // Step 3: Configure virtual CKYC response
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST101"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {
                                                  "customerId": "CUST101",
                                                  "kycStatus": "VERIFIED",
                                                  "ckycNumber": "CKYC123456789",
                                                  "remarks": "KYC verification successful"
                                                }
                                                """)
                        )
        );
        // Scenario 2: Customer with pending KYC
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST102"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                        {
                                          "customerId": "CUST102",
                                          "kycStatus": "PENDING",
                                          "ckycNumber": null,
                                          "remarks": "Documents are under verification"
                                        }
                                        """)
                        )
        );
        // Scenario 3: Customer with rejected KYC
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST103"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                        {
                                          "customerId": "CUST103",
                                          "kycStatus": "REJECTED",
                                          "ckycNumber": null,
                                          "remarks": "Submitted documents failed verification"
                                        }
                                        """)
                        )
        );
        // Scenario 4: Customer not found in CKYC Registry
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST104"))
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                        {
                                          "error": "CUSTOMER_NOT_FOUND",
                                          "message": "Customer does not exist in CKYC Registry"
                                        }
                                        """)
                        )
        );
        // Scenario 5: CKYC Registry temporarily unavailable
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST105"))
                        .willReturn(
                                aResponse()
                                        .withStatus(503)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                        {
                                          "error": "SERVICE_UNAVAILABLE",
                                          "message": "CKYC Registry is temporarily unavailable"
                                        }
                                        """)
                        )
        );
        // Scenario 6: CKYC Registry responds slowly
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST106"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withFixedDelay(5000)
                                        .withBody("""
                                        {
                                          "customerId": "CUST106",
                                          "kycStatus": "VERIFIED",
                                          "ckycNumber": "CKYC106789",
                                          "remarks": "KYC verification successful"
                                        }
                                        """)
                        )
        );
        // Scenario 7: CKYC Registry returns malformed JSON
        wireMockServer.stubFor(
                get(urlEqualTo("/ckyc/verify/CUST107"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                        {
                                          "customerId": "CUST107",
                                          "kycStatus": "VERIFIED",
                                          "ckycNumber":
                                        }
                                        """)
                        )
        );
        System.out.println("==================================");
        System.out.println("Virtual CKYC Registry Started!");
        System.out.println("Running on: http://localhost:8081");
        System.out.println("Customer CUST101 -> VERIFIED");
        System.out.println("==================================");
    }
}