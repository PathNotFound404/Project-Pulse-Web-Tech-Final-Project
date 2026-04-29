package com.tcu.projectpulse.peerevaluation;

import com.tcu.projectpulse.peerevaluation.repository.PeerEvaluationRepository;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.section.repository.SectionRepository;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeerEvaluationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired private PeerEvaluationRepository peerEvaluationRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private SectionRepository sectionRepository;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final String RAW_PASSWORD = "Password1!";
    private static final String WEEK =
            LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).toString();

    private RestClient http;
    private Section section;
    private Student alice;
    private Student bob;
    private Student carol;
    private String sessionCookie;

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);

        section = sectionRepository.save(new Section("2024-2025"));

        alice = new Student();
        alice.setFirstName("Alice");
        alice.setLastName("Adams");
        alice.setEmail("alice.pe@tcu.edu");
        alice.setSection(section);
        alice.setPasswordHash(encoder.encode(RAW_PASSWORD));
        alice = studentRepository.save(alice);

        bob = new Student();
        bob.setFirstName("Bob");
        bob.setLastName("Baker");
        bob.setEmail("bob.pe@tcu.edu");
        bob.setSection(section);
        bob.setPasswordHash(encoder.encode(RAW_PASSWORD));
        bob = studentRepository.save(bob);

        carol = new Student();
        carol.setFirstName("Carol");
        carol.setLastName("Clark");
        carol.setEmail("carol.pe@tcu.edu");
        carol.setSection(section);
        carol.setPasswordHash(encoder.encode(RAW_PASSWORD));
        carol = studentRepository.save(carol);

        Team team = new Team();
        team.setName("Team PE");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(alice, bob, carol)));
        team.setInstructors(new ArrayList<>());
        teamRepository.save(team);

        ResponseEntity<Map> loginResponse = http.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", "alice.pe@tcu.edu", "password", RAW_PASSWORD))
                .retrieve().toEntity(Map.class);
        sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
    }

    @AfterEach
    void tearDown() {
        peerEvaluationRepository.deleteAll();
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        sectionRepository.deleteAll();
    }

    // ── GET /api/peer-evaluations/team ────────────────────────────────────────

    @Test
    void getSheet_authenticatedStudentOnTeam_returns200WithTeammates() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/peer-evaluations/team?week=" + WEEK)
                .header(HttpHeaders.COOKIE, sessionCookie)
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        List<?> entries = (List<?>) data.get("entries");
        assertThat(entries).hasSize(3);
    }

    @Test
    void getSheet_unauthenticated_returns401() {
        ResponseEntity<Map> response = http.get()
                .uri("/api/peer-evaluations/team?week=" + WEEK)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void getSheet_studentNotOnTeam_returns400() {
        Student dave = new Student();
        dave.setFirstName("Dave");
        dave.setLastName("Davis");
        dave.setEmail("dave.pe@tcu.edu");
        dave.setSection(section);
        dave.setPasswordHash(encoder.encode(RAW_PASSWORD));
        studentRepository.save(dave);

        ResponseEntity<Map> loginResponse = http.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", "dave.pe@tcu.edu", "password", RAW_PASSWORD))
                .retrieve().toEntity(Map.class);
        String daveCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];

        ResponseEntity<Map> response = http.get()
                .uri("/api/peer-evaluations/team?week=" + WEEK)
                .header(HttpHeaders.COOKIE, daveCookie)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    // ── POST /api/peer-evaluations ────────────────────────────────────────────

    private List<Map<String, Object>> buildEntries(int score) {
        return List.of(
                buildEntry(alice.getId(), score),
                buildEntry(bob.getId(), score),
                buildEntry(carol.getId(), score)
        );
    }

    private Map<String, Object> buildEntry(Long evaluateeId, int score) {
        return Map.of(
                "evaluateeId", evaluateeId,
                "qualityOfWork", score,
                "productivity", score,
                "proactiveness", score,
                "treatsOthersWithRespect", score,
                "handlesCriticism", score,
                "performanceInMeetings", score,
                "publicComment", "Good work",
                "privateComment", "Private note"
        );
    }

    @Test
    void submit_validScores_returns200() {
        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(7)))
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void submit_unauthenticated_returns401() {
        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(7)))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void submit_missingTeammate_returns400() {
        List<Map<String, Object>> incomplete = List.of(
                buildEntry(alice.getId(), 7),
                buildEntry(bob.getId(), 7)
                // carol omitted
        );

        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", incomplete))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void submit_scoreZero_returns400() {
        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(0)))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void submit_scoreEleven_returns400() {
        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(11)))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void submit_resubmissionSameWeek_returns200() {
        http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(5)))
                .retrieve().toEntity(Map.class);

        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(8)))
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void submit_privateCommentNotInResponse() {
        ResponseEntity<Map> response = http.post().uri("/api/peer-evaluations")
                .header(HttpHeaders.COOKIE, sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("week", WEEK, "entries", buildEntries(7)))
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        List<?> entries = (List<?>) data.get("entries");
        for (Object e : entries) {
            assertThat(((Map<?, ?>) e).containsKey("privateComment")).isFalse();
        }
    }
}
