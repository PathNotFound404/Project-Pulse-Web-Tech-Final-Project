package com.tcu.projectpulse.instructor;

import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
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

    // -------------------------------------------------------
    // CODY'S TESTS (UC-18, 21, 22, 23, 24)
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // EDUARDA'S TESTS (UC-30, 31, 32, 33, 34)
    // -------------------------------------------------------

    // UC-30: Register via valid invite token
    @Test
    void registerInstructor_validToken_returns200() {
        InvitationToken token = new InvitationToken("test-token-abc", "newteacher@tcu.edu");
        invitationTokenRepository.save(token);

        ResponseEntity<Map> response = http.post()
                .uri("/api/instructors/register?token=test-token-abc")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"firstName\":\"New\",\"lastName\":\"Teacher\",\"password\":\"pass123\",\"reenterPassword\":\"pass123\"}")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("firstName")).isEqualTo("New");
        assertThat(data.get("email")).isEqualTo("newteacher@tcu.edu");
    }

    @Test
    void registerInstructor_invalidToken_returns404() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/instructors/register?token=bad-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"firstName\":\"New\",\"lastName\":\"Teacher\",\"password\":\"pass123\",\"reenterPassword\":\"pass123\"}")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    // UC-31: Peer eval report for section — empty since no data in test DB
    @Test
    void getPeerEvalReportForSection_noData_returnsEmptyList() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/instructors/reports/peer-evaluation/section/1?activeWeek=02-12-2024 to 02-18-2024")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).isEmpty();
    }

    // UC-32: WAR report for team — fixed: activeWeek must be MM-dd-yyyy only
    @Test
    void getWarReportForTeam_noData_returnsEmptyList() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/instructors/reports/war/team/1?activeWeek=02-12-2024")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).isEmpty();
    }

    // UC-33: Peer eval report for student — empty since no data in test DB
    @Test
    void getPeerEvalReportForStudent_noData_returnsEmptyList() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/instructors/reports/peer-evaluation/student/1?startWeek=02-12-2024&endWeek=04-28-2024")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).isEmpty();
    }

    // UC-34: WAR report for student — empty since no data in test DB
    @Test
    void getWarReportForStudent_noData_returnsEmptyList() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/instructors/reports/war/student/1?startWeek=02-12-2024&endWeek=04-28-2024")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).isEmpty();
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