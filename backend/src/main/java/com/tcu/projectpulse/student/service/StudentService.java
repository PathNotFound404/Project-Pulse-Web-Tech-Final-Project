package com.tcu.projectpulse.student.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.dto.*;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeamRepository teamRepository;

    public StudentService(StudentRepository studentRepository, TeamRepository teamRepository) {
        this.studentRepository = studentRepository;
        this.teamRepository = teamRepository;
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
