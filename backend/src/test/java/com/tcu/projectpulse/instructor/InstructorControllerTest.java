package com.tcu.projectpulse.instructor;

import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InstructorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private InvitationTokenRepository invitationTokenRepository;

    private RestClient http;
    private Instructor activeInstructor;
    private Instructor deactivatedInstructor;

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);
        activeInstructor = instructorRepository.save(buildInstructor("Jane", "Smith", "j.smith@tcu.edu", InstructorStatus.ACTIVE));
        deactivatedInstructor = instructorRepository.save(buildInstructor("Bob", "Jones", "b.jones@tcu.edu", InstructorStatus.DEACTIVATED));
    }

    @AfterEach
    void tearDown() {
        invitationTokenRepository.deleteAll();
        instructorRepository.deleteAll();
    }

    @Test
    void findInstructors_noParams_returnsAll() {
        ResponseEntity<Map> response = http.get().uri("/api/instructors")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findInstructors_filterByStatusActive_returnsOnlyActive() {
        ResponseEntity<Map> response = http.get().uri("/api/instructors?status=ACTIVE")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<?, ?>> data = (List<Map<?, ?>>) response.getBody().get("data");
        assertThat(data).allSatisfy(i -> assertThat(i.get("status")).isEqualTo("ACTIVE"));
    }

    @Test
    void getInstructor_exists_returnsDetails() {
        ResponseEntity<Map> response = http.get().uri("/api/instructors/" + activeInstructor.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("firstName")).isEqualTo("Jane");
        assertThat(data.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void getInstructor_notFound_returns404() {
        ResponseEntity<Map> response = http.get().uri("/api/instructors/9999")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Integer) response.getBody().get("code")).isEqualTo(404);
    }

    @Test
    void inviteInstructors_validEmails_returnsLinks() {
        ResponseEntity<Map> response = http.post().uri("/api/instructors/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"emails\":[\"new@tcu.edu\"]}")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        List<Map<?, ?>> data = (List<Map<?, ?>>) response.getBody().get("data");
        assertThat(data).hasSize(1);
        assertThat((String) data.get(0).get("inviteLink")).contains("token=");
    }

    @Test
    void deactivateInstructor_active_becomesDeactivated() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/instructors/" + activeInstructor.getId() + "/deactivate")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("DEACTIVATED");
    }

    @Test
    void deactivateInstructor_alreadyDeactivated_returns400() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/instructors/" + deactivatedInstructor.getId() + "/deactivate")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void reactivateInstructor_deactivated_becomesActive() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/instructors/" + deactivatedInstructor.getId() + "/reactivate")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void reactivateInstructor_alreadyActive_returns400() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/instructors/" + activeInstructor.getId() + "/reactivate")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Instructor buildInstructor(String firstName, String lastName, String email, InstructorStatus status) {
        Instructor i = new Instructor();
        i.setFirstName(firstName);
        i.setLastName(lastName);
        i.setEmail(email);
        i.setStatus(status);
        i.setTeams(new ArrayList<>());
        return i;
    }
}
