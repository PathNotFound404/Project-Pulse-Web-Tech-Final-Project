package com.tcu.projectpulse.auth;

import com.tcu.projectpulse.student.domain.StudentInvitationToken;
import com.tcu.projectpulse.student.repository.StudentInvitationTokenRepository;
import com.tcu.projectpulse.student.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentRegistrationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentInvitationTokenRepository tokenRepository;

    private RestClient http;
    private String validToken;

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);
        validToken = UUID.randomUUID().toString();
        tokenRepository.save(new StudentInvitationToken(validToken, "invited@example.com"));
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    @Test
    void register_validTokenAndRequest_returns201WithFlagTrue() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "lastName", "Smith",
                        "email", "jane.smith@example.com",
                        "password", "securePass1"
                ))
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void register_tokenAlreadyUsed_returns409WithFlagFalse() {
        StudentInvitationToken token = tokenRepository.findByToken(validToken).get();
        token.setUsed(true);
        tokenRepository.save(token);

        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "lastName", "Smith",
                        "email", "jane.smith@example.com",
                        "password", "securePass1"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void register_missingFirstName_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "lastName", "Smith",
                        "email", "jane.smith@example.com",
                        "password", "securePass1"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void register_missingLastName_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "email", "jane.smith@example.com",
                        "password", "securePass1"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void register_missingEmail_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "lastName", "Smith",
                        "password", "securePass1"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void register_invalidEmailFormat_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "lastName", "Smith",
                        "email", "notanemail",
                        "password", "securePass1"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void register_passwordTooShort_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/auth/register/student?token=" + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Jane",
                        "lastName", "Smith",
                        "email", "jane.smith@example.com",
                        "password", "short"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }
}
