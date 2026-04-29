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

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

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

    public List<InviteLinkDto> generateInviteLinks(List<String> emails) {
        List<InviteLinkDto> links = new ArrayList<>();
        for (String email : emails) {
            String trimmed = email.trim();
            if (trimmed.isBlank()) continue;

            String token = UUID.randomUUID().toString();
            tokenRepository.save(new StudentInvitationToken(token, trimmed));

            String link = frontendBaseUrl + "/register?token=" + token;
            links.add(new InviteLinkDto(trimmed, link));
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

        // TODO: restore strict token validation before production — any non-empty token accepted for local testing
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Invalid or expired invitation token");

        String registrationEmail = request.email().trim();
        java.util.Optional<StudentInvitationToken> maybeToken = tokenRepository.findByToken(token);
        if (maybeToken.isPresent()) {
            StudentInvitationToken invitationToken = maybeToken.get();
            if (invitationToken.isUsed() || studentRepository.existsByEmail(invitationToken.getEmail())) {
                throw new AccountAlreadyExistsException("Account already created. Please log in.");
            }
            registrationEmail = invitationToken.getEmail();
            invitationToken.setUsed(true);
        } else if (studentRepository.existsByEmail(registrationEmail)) {
            throw new AccountAlreadyExistsException("Account already created. Please log in.");
        }

        Student student = new Student();
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(registrationEmail);
        student.setPasswordHash(passwordEncoder.encode(request.password()));

        student = studentRepository.save(student);
        return toDetailDto(student);
    }

    // UC-26: Student logs in
    public Long login(String email, String password) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email is required");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required");

        Student student = studentRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(password, student.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password");

        return student.getId();
    }

    // UC-26: Get logged-in student's profile
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));
        return new UserProfileDto(student.getFirstName(), student.getLastName(), student.getEmail());
    }

    // UC-26: Student edits her account
    public UserProfileDto update(Long id, UpdateStudentRequest request) {
        if (request.firstName() == null || request.firstName().isBlank())
            throw new IllegalArgumentException("First name is required");
        if (request.lastName() == null || request.lastName().isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (request.email() == null || request.email().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (!request.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Invalid email format");

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Student", id));

        if (!request.email().equalsIgnoreCase(student.getEmail()) && studentRepository.existsByEmail(request.email().trim())) {
            throw new AccountAlreadyExistsException("Email " + request.email() + " is already in use");
        }

        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(request.email().trim());

        return new UserProfileDto(student.getFirstName(), student.getLastName(), student.getEmail());
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
