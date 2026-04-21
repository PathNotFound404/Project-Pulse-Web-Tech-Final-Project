package com.tcu.projectpulse.student;

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
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SectionRepository sectionRepository;

    private RestClient http;
    private Section section;
    private Team team;
    private Student student;

    @BeforeEach
    void setUp() {
        http = RestClient.create("http://localhost:" + port);

        section = sectionRepository.save(new Section("2024-2025"));

        student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@tcu.edu");
        student.setSection(section);
        student = studentRepository.save(student);

        team = new Team();
        team.setName("Team Alpha");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(student)));
        team.setInstructors(new ArrayList<>());
        team = teamRepository.save(team);
    }

    @AfterEach
    void tearDown() {
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        sectionRepository.deleteAll();
    }

    @Test
    void findStudents_noParams_returnsAllStudents() {
        ResponseEntity<Map> response = http.get().uri("/api/students")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
        assertThat((List<?>) response.getBody().get("data")).isNotEmpty();
    }

    @Test
    void findStudents_byFirstName_returnsMatch() {
        ResponseEntity<Map> response = http.get().uri("/api/students?firstName=John")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> data = (List<?>) response.getBody().get("data");
        assertThat(data).isNotEmpty();
    }

    @Test
    void findStudents_noMatch_returnsEmptyList() {
        ResponseEntity<Map> response = http.get().uri("/api/students?firstName=Nobody")
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> data = (List<?>) response.getBody().get("data");
        assertThat(data).isEmpty();
    }

    @Test
    void getStudent_exists_returnsDetails() {
        ResponseEntity<Map> response = http.get().uri("/api/students/" + student.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("firstName")).isEqualTo("John");
        assertThat(data.get("sectionName")).isEqualTo("2024-2025");
    }

    @Test
    void getStudent_notFound_returns404() {
        ResponseEntity<Map> response = http.get().uri("/api/students/9999")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }

    @Test
    void deleteStudent_exists_returns200() {
        ResponseEntity<Map> response = http.delete().uri("/api/students/" + student.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void deleteStudent_notFound_returns404() {
        ResponseEntity<Map> response = http.delete().uri("/api/students/9999")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeStudentFromTeam_studentOnTeam_returns200() {
        ResponseEntity<Map> response = http.delete()
                .uri("/api/teams/" + team.getId() + "/students/" + student.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void removeStudentFromTeam_studentNotOnTeam_returns400() {
        Student other = new Student();
        other.setFirstName("Jane");
        other.setLastName("Other");
        other.setEmail("jane.other@tcu.edu");
        other.setSection(section);
        other = studentRepository.save(other);
        final Long otherId = other.getId();

        ResponseEntity<Map> response = http.delete()
                .uri("/api/teams/" + team.getId() + "/students/" + otherId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }
}
