package com.tcu.projectpulse.war;

import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.section.repository.SectionRepository;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.war.domain.Activity;
import com.tcu.projectpulse.war.domain.ActivityCategory;
import com.tcu.projectpulse.war.domain.ActivityStatus;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.repository.ActivityRepository;
import com.tcu.projectpulse.war.repository.WarRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WarControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private WarRepository warRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private RestClient http;
    private Student student;
    private Section section;
    private War war;
    private Activity activity;
    private String sessionCookie;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final String RAW_PASSWORD = "Password1!";

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);

        section = sectionRepository.save(new Section("2026-Spring"));

        student = new Student();
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.war@example.com");
        student.setPasswordHash(encoder.encode(RAW_PASSWORD));
        student.setSection(section);
        student = studentRepository.save(student);

        ResponseEntity<Map> loginResponse = http.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", "jane.war@example.com", "password", RAW_PASSWORD))
                .retrieve()
                .toEntity(Map.class);
        sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        war = new War();
        war.setStudent(student);
        war.setWeekStart(monday);
        war.setWeekEnd(monday.plusDays(6));
        war = warRepository.save(war);

        activity = new Activity();
        activity.setWar(war);
        activity.setCategory(ActivityCategory.DEVELOPMENT);
        activity.setDescription("Initial activity");
        activity.setPlannedHours(2.0);
        activity.setActualHours(0.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity = activityRepository.save(activity);
    }

    @AfterEach
    void tearDown() {
        activityRepository.deleteAll();
        warRepository.deleteAll();
        studentRepository.deleteAll();
        sectionRepository.deleteAll();
    }

    // ── GET /api/wars?week=YYYY-MM-DD ─────────────────────────────────────────

    @Test
    void getWar_authenticated_returns200WithActivities() {
        String currentMonday = LocalDate.now().with(DayOfWeek.MONDAY).toString();

        ResponseEntity<Map> response = http.get()
                .uri("/api/wars?week=" + currentMonday)
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat((List<?>) data.get("activities")).isNotEmpty();
    }

    @Test
    void getWar_unauthenticated_returns401WithFlagFalse() {
        String currentMonday = LocalDate.now().with(DayOfWeek.MONDAY).toString();

        ResponseEntity<Map> response = http.get()
                .uri("/api/wars?week=" + currentMonday)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void getWar_futureWeek_returns400WithFlagFalse() {
        String futureMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY).toString();

        ResponseEntity<Map> response = http.get()
                .uri("/api/wars?week=" + futureMonday)
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    // ── POST /api/wars/{warId}/activities ─────────────────────────────────────

    @Test
    void addActivity_validRequest_returns201WithActivity() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "TESTING",
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("category")).isEqualTo("TESTING");
        assertThat(data.get("description")).isEqualTo("Write unit tests");
    }

    @Test
    void addActivity_unauthenticated_returns401WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "TESTING",
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_missingCategory_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_missingDescription_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "DEVELOPMENT",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_negativePlannedHours_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "DEVELOPMENT",
                        "description", "Write unit tests",
                        "plannedHours", -1.0,
                        "actualHours", 0.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_negativeActualHours_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "DEVELOPMENT",
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", -1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_invalidCategory_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "BADCATEGORY",
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "IN_PROGRESS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void addActivity_invalidStatus_returns400WithFlagFalse() {
        ResponseEntity<Map> response = http.post()
                .uri("/api/wars/" + war.getId() + "/activities")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "DEVELOPMENT",
                        "description", "Write unit tests",
                        "plannedHours", 3.0,
                        "actualHours", 1.0,
                        "status", "BADSTATUS"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    // ── PUT /api/wars/{warId}/activities/{activityId} ─────────────────────────

    @Test
    void updateActivity_validRequest_returns200WithUpdatedActivity() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/wars/" + war.getId() + "/activities/" + activity.getId())
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "TESTING",
                        "description", "Updated description",
                        "plannedHours", 4.0,
                        "actualHours", 2.0,
                        "status", "DONE"
                ))
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("category")).isEqualTo("TESTING");
        assertThat(data.get("description")).isEqualTo("Updated description");
        assertThat(data.get("status")).isEqualTo("DONE");
    }

    @Test
    void updateActivity_unauthenticated_returns401WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/wars/" + war.getId() + "/activities/" + activity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "TESTING",
                        "description", "Updated description",
                        "plannedHours", 4.0,
                        "actualHours", 2.0,
                        "status", "DONE"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void updateActivity_notFound_returns404WithFlagFalse() {
        ResponseEntity<Map> response = http.put()
                .uri("/api/wars/" + war.getId() + "/activities/99999")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "category", "TESTING",
                        "description", "Updated description",
                        "plannedHours", 4.0,
                        "actualHours", 2.0,
                        "status", "DONE"
                ))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    // ── DELETE /api/wars/{warId}/activities/{activityId} ──────────────────────

    @Test
    void deleteActivity_existingActivity_returns200WithFlagTrue() {
        ResponseEntity<Map> response = http.delete()
                .uri("/api/wars/" + war.getId() + "/activities/" + activity.getId())
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void deleteActivity_unauthenticated_returns401WithFlagFalse() {
        ResponseEntity<Map> response = http.delete()
                .uri("/api/wars/" + war.getId() + "/activities/" + activity.getId())
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void deleteActivity_notFound_returns404WithFlagFalse() {
        ResponseEntity<Map> response = http.delete()
                .uri("/api/wars/" + war.getId() + "/activities/99999")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }
}
