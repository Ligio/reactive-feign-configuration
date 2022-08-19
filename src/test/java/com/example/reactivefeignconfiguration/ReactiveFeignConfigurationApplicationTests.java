package com.example.reactivefeignconfiguration;

import com.example.reactivefeignconfiguration.client.ExampleClient;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import feign.FeignException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WireMockTest(httpPort = 8081)
class ReactiveFeignConfigurationApplicationTests {

  @Autowired
  ExampleClient client;

  @Test
  void internalServerErrorIsManagedAsInternalServerErrorException() {
    stubFor(get(urlEqualTo("/healthcheck"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR))
    );

    // reactivefeign 3.2.5 will throw *FeignException.InternalServerError*
    // but if retrier is enabled, it's wrapped in *OutOfRetriesException*.
    // It would be better to have the same behaviour regardless the retrier is enabled or not.
    // To verify the inconsistency here, comment-out the Retry bean in ExampleClientConfiguration class
    StepVerifier.create(client.healthcheck())
        .expectError(FeignException.InternalServerError.class)
        .verify();
  }

  @Test
  void serviceUnavailableResponseShouldBeRetried() {
    // first call: error 503 ==> should be retried
    stubFor(get(urlEqualTo("/healthcheck"))
        .inScenario("Retry-Scenario")
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_SERVICE_UNAVAILABLE))
        .willSetStateTo("error-returned")
    );

    // second call: 200 ==> should be the final result
    stubFor(get(urlEqualTo("/healthcheck"))
        .inScenario("Retry-Scenario")
        .whenScenarioStateIs("error-returned")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK))
    );

    // reactivefeign 3.2.5 ==> test is successful
    // reactivefeign 3.2.3 ==> test fails since ErrorDecoder is not used
    //                         and the Exception is not wrapped in a RetryableException
    StepVerifier.create(client.healthcheck())
        .expectNextMatches(response -> response.status() == HttpStatus.SC_OK)
        .verifyComplete();
  }

  @Test
  void errors_NOT_ServiceUnavailableShould_NOT_BeRetried() {
    // first call: error 500 ==> should NOT be retried
    stubFor(get(urlEqualTo("/healthcheck"))
        .inScenario("Retry-Scenario")
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR))
        .willSetStateTo("error-returned")
    );

    // second call: 200 ==> should NOT be called
    stubFor(get(urlEqualTo("/healthcheck"))
        .inScenario("Retry-Scenario")
        .whenScenarioStateIs("error-returned")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK))
    );

    // reactivefeign 3.2.5 ==> test fails. It seems all Exceptions will be wrapped as RetryableException
    // reactivefeign 3.2.3 ==> test is successful
    StepVerifier.create(client.healthcheck())
        .expectNextMatches(response -> response.status() == HttpStatus.SC_INTERNAL_SERVER_ERROR)
        .verifyComplete();
  }

}
