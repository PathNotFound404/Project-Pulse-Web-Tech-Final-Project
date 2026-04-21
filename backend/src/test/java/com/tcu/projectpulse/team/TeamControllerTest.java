package com.tcu.projectpulse.team;

import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
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
class TeamControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private SectionRepository sectionRepository;

    private RestClient http;
    private Section section;
    private Team team;
    private Student student;
    private Instructor instructor;

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

        instructor = new Instructor();
        instructor.setFirstName("Jane");
        instructor.setLastName("Smith");
        instructor.setEmail("j.smith@tcu.edu");
        instructor.setStatus(InstructorStatus.ACTIVE);
        instructor.setTeams(new ArrayList<>());
        instructor = instructorRepository.save(instructor);

        team = new Team();
        team.setName("Team Alpha");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(student)));
        team.setInstructors(new ArrayList<>(List.of(instructor)));
        team = teamRepository.save(team);
    }

    @AfterEach
    void tearDown() {
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        instructorRepository.deleteAll();
        sectionRepository.deleteAll();
    }

    @Test
    void deleteTeam_exists_returns200() {
        ResponseEntity<Map> response = http.delete().uri("/api/teams/" + team.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void deleteTeam_notFound_returns404() {
        ResponseEntity<Map> response = http.delete().uri("/api/teams/9999")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Integer) response.getBody().get("code")).isEqualTo(404);
    }

    @Test
    void assignInstructors_validIds_returns200() {
        Instructor newInstructor = new Instructor();
        newInstructor.setFirstName("Bob");
        newInstructor.setLastName("Jones");
        newInstructor.setEmail("b.jones@tcu.edu");
        newInstructor.setStatus(InstructorStatus.ACTIVE);
        newInstructor.setTeams(new ArrayList<>());
        newInstructor = instructorRepository.save(newInstructor);

        String body = "{\"instructorIds\":[" + newInstructor.getId() + "]}";
        ResponseEntity<Map> response = http.put()
                .uri("/api/teams/" + team.getId() + "/instructors")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void assignInstructors_invalidIds_returns400() {
        String body = "{\"instructorIds\":[9999]}";
        ResponseEntity<Map> response = http.put()
                .uri("/api/teams/" + team.getId() + "/instructors")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void removeInstructor_instructorOnTeam_returns200() {
        ResponseEntity<Map> response = http.delete()
                .uri("/api/teams/" + team.getId() + "/instructors/" + instructor.getId())
                .retrieve().toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) response.getBody().get("flag")).isTrue();
    }

    @Test
    void removeInstructor_notOnTeam_returns400() {
        Instructor notOnTeam = new Instructor();
        notOnTeam.setFirstName("Not");
        notOnTeam.setLastName("OnTeam");
        notOnTeam.setEmail("not.onteam@tcu.edu");
        notOnTeam.setStatus(InstructorStatus.ACTIVE);
        notOnTeam.setTeams(new ArrayList<>());
        notOnTeam = instructorRepository.save(notOnTeam);

        ResponseEntity<Map> response = http.delete()
                .uri("/api/teams/" + team.getId() + "/instructors/" + notOnTeam.getId())
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Boolean) response.getBody().get("flag")).isFalse();
    }
}
