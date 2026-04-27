package com.tcu.projectpulse.student;

import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentAccountControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    private RestClient http;
    private Student student;
    private String sessionCookie;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final String RAW_PASSWORD = "Password1!";

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);

        student = new Student();
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");
        student.setPasswordHash(encoder.encode(RAW_PASSWORD));
        student = studentRepository.save(student);

        ResponseEntity<Map> loginResponse = http.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", "jane.smith@example.com", "password", RAW_PASSWORD))
                .retrieve()
                .toEntity(Map.class);

        String setCookieHeader = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionCookie = setCookieHeader.split(";")[0];
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
    }

    @Test
    void getMe_authenticated_returns200WithProfile() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("firstName")).isEqualTo("Jane");
        assertThat(data.get("lastName")).isEqualTo("Smith");
        assertThat(data.get("email")).isEqualTo("jane.smith@example.com");
    }

    @Test
    void getMe_unauthenticated_returns401WithFlagFalse() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/users/me")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateMe_validRequest_returns200WithUpdatedValues() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Janet",
                        "lastName", "Jones",
                        "email", "janet.jones@example.com"
                ))
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("firstName")).isEqualTo("Janet");
        assertThat(data.get("lastName")).isEqualTo("Jones");
        assertThat(data.get("email")).isEqualTo("janet.jones@example.com");
    }

    @Test
    void updateMe_unauthenticated_returns401WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Janet",
                        "lastName", "Jones",
                        "email", "janet.jones@example.com"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateMe_missingFirstName_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "lastName", "Jones",
                        "email", "janet.jones@example.com"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateMe_missingLastName_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Janet",
                        "email", "janet.jones@example.com"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateMe_invalidEmailFormat_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Janet",
                        "lastName", "Jones",
                        "email", "notanemail"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateMe_emailTakenByAnotherStudent_returns409WithFlagFalse() {
        Student other = new Student();
        other.setFirstName("Other");
        other.setLastName("User");
        other.setEmail("other.user@example.com");
        other.setPasswordHash(encoder.encode("irrelevant"));
        studentRepository.save(other);

        ResponseEntity<Map> response = http.put()
                .uri("/api/users/me")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "firstName", "Janet",
                        "lastName", "Jones",
                        "email", "other.user@example.com"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }
}
