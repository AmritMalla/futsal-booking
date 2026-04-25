package com.amrit.futsal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("kubernetes")
class KubernetesProfileTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void livenessEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/health/liveness", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void readinessEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/health/readiness", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void prometheusEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/metrics", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).contains("jvm.memory.used");
    }
}
