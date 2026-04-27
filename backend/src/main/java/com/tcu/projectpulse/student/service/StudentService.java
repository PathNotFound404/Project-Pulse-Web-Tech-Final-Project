package com.tcu.projectpulse.student.service;

import com.tcu.projectpulse.common.exception.AccountAlreadyExistsException;
import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.domain.StudentInvitationToken;
import com.tcu.projectpulse.student.dto.*;
import com.tcu.projectpulse.student.repository.StudentInvitationTokenRepository;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeamRepository teamRepository;
    private final StudentInvitationTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${server.port:8080}")
    private int serverPort;

    public StudentService(StudentRepository studentRepository,
                          TeamRepository teamRepository,
                          StudentInvitationTokenRepository tokenRepository) {
        this.studentRepository = studentRepository;
        this.teamRepository = teamRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public List<StudentSummaryDto> findStudents(StudentSearchCriteria criteria) {
        StudentSpecification spec = new StudentSpecification(criteria);
        List<Student> students = studentRepository.findAll(spec);

        return students.stream()
                .sorted(Comparator
                        .comparing((Student s) -> s.getSection() != null ? s.getSection().getName() : "",
                                Comparator.reverseOrder())
                        .thenComparing(Student::getLastName))
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentDetailDto findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Student", id));
        return toDetailDto(student);
    }

    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Student", id));

        // Remove student from all teams before deleting
        for (Team team : student.getTeams()) {
            team.getStudents().remove(student);
        }
        student.getTeams().clear();

        studentRepository.delete(student);
    }

    public void removeFromTeam(Long teamId, Long studentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));

        if (!team.getStudents().contains(student)) {
            throw new IllegalStateException("Student " + studentId + " is not a member of team " + teamId);
        }

        team.getStudents().remove(student);
        student.getTeams().remove(team);
    }

    // UC-18 equivalent: generate student invite links
    public List<StudentInviteLinkDto> generateInviteLinks(List<String> emails) {
        List<StudentInviteLinkDto> links = new ArrayList<>();
        for (String email : emails) {
            String trimmed = email.trim();
            if (trimmed.isBlank()) continue;

            String token = UUID.randomUUID().toString();
            tokenRepository.save(new StudentInvitationToken(token, trimmed));

            String link = "http://localhost:" + serverPort + "/api/students/register?token=" + token;
            links.add(new StudentInviteLinkDto(trimmed, link));
        }
        return links;
    }

    // UC-25: Student sets up a student account
    public StudentDetailDto register(String token, RegisterStudentRequest request) {
        if (request.firstName() == null || request.firstName().isBlank())
            throw new IllegalArgumentException("First name is required");
        if (request.lastName() == null || request.lastName().isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (request.email() == null || request.email().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (!request.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Invalid email format");
        if (request.password() == null || request.password().isBlank())
            throw new IllegalArgumentException("Password is required");
        if (request.password().length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters");

        StudentInvitationToken invitationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation token"));

        if (invitationToken.isUsed() || studentRepository.existsByEmail(invitationToken.getEmail())) {
            throw new AccountAlreadyExistsException("Account already created. Please log in.");
        }

        Student student = new Student();
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(invitationToken.getEmail());
        student.setPasswordHash(passwordEncoder.encode(request.password()));

        student = studentRepository.save(student);
        invitationToken.setUsed(true);

        return toDetailDto(student);
    }

    // UC-26: Student edits an account
    public StudentDetailDto update(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Student", id));

        if (request.firstName() != null && !request.firstName().isBlank())
            student.setFirstName(request.firstName().trim());
        if (request.lastName() != null && !request.lastName().isBlank())
            student.setLastName(request.lastName().trim());
        if (request.email() != null && !request.email().isBlank()) {
            if (!request.email().equals(student.getEmail()) && studentRepository.existsByEmail(request.email())) {
                throw new IllegalStateException("Email " + request.email() + " is already in use");
            }
            student.setEmail(request.email().trim());
        }

        return toDetailDto(student);
    }

    private StudentSummaryDto toSummaryDto(Student student) {
        String teamName = student.getTeams().isEmpty() ? null : student.getTeams().get(0).getName();
        return new StudentSummaryDto(student.getId(), student.getFirstName(), student.getLastName(), teamName);
    }

    private StudentDetailDto toDetailDto(Student student) {
        String sectionName = student.getSection() != null ? student.getSection().getName() : null;
        List<String> teamNames = student.getTeams().stream().map(Team::getName).toList();
        return new StudentDetailDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                sectionName,
                teamNames,
                student.getWars().size(),
                student.getPeerEvaluations().size()
        );
    }
}
