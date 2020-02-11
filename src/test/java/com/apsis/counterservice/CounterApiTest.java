package com.apsis.counterservice;

import com.apsis.counterservice.api.model.Counter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {CounterServiceApplication.class})
public class CounterApiTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private WebTestClient webTestClient;

    @Test
    @Order(1)
    public void givenCounter_whenCreateCounter_thenSuccess() {
        Counter counter = Counter.builder().name("test-counter").build();

        webTestClient
                .post().uri(uriBuilder -> uriBuilder.path("/api/counters").build())
                .headers(httpHeaders -> httpHeaders.add("accept-type", MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(counter))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Counter.class).isEqualTo(counter);
    }

    @Test
    @Order(2)
    public void givenExistingCounter_whenGetCounter_thenSuccess() {
        Counter counter = Counter.builder().name("test-counter").build();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/counters/" + counter.getName()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Counter.class).isEqualTo(counter);
    }

    @Test
    @Order(3)
    public void givenCounters_whenGetAllCounters_thenSuccess() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/counters").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Counter.class).hasSize(1);
    }

    @Test
    @Order(4)
    public void givenCounter_whenUpdateCounter_thenSuccess() {
        Counter counter = Counter.builder().name("test-counter").build();

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder.path("/api/counters/" + counter.getName()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Counter.class).value(counter1 -> Assertions.assertEquals(1, counter1.getValue()));
    }

    @Test
    @Order(5)
    public void givenAnExistingCounter_whenCreateCounter_thenError() {
        Counter counter = Counter.builder().name("test-counter").build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/counters").build())
                .body(BodyInserters.fromValue(counter))
                .exchange()
                .expectStatus().value(integer -> Assertions.assertEquals(409, integer));
    }

    @Test
    public void givenNonExistingCounter_whenGetCounter_thenError() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/counters/invalid").build())
                .exchange()
                .expectStatus().value(integer -> Assertions.assertEquals(404, integer));
    }

    @Test
    public void givenNonExistingCounter_whenUpdateCounter_thenError() {
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder.path("/api/counters/invalid").build())
                .exchange()
                .expectStatus().value(integer -> Assertions.assertEquals(404, integer));
    }

    @Test
    public void givenInvalidInput_whenCreateCounter_thenError() {
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/counters").build())
                .body(BodyInserters.fromValue(Counter.builder().name("").build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

}
