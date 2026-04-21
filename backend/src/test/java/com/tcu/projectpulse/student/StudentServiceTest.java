package com.tcu.projectpulse.student;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.dto.StudentDetailDto;
import com.tcu.projectpulse.student.dto.StudentSearchCriteria;
import com.tcu.projectpulse.student.dto.StudentSummaryDto;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.student.service.StudentService;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private StudentService studentService;

    private Section section;
    private Team team;
    private Student student;

    @BeforeEach
    void setUp() {
        section = new Section("2024-2025");
        section.setId(1L);

        team = new Team();
        team.setId(1L);
        team.setName("Team Alpha");
        team.setSection(section);

        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@tcu.edu");
        student.setSection(section);
        student.setTeams(new ArrayList<>(List.of(team)));

        team.setStudents(new ArrayList<>(List.of(student)));
    }

    @Test
    void findStudents_withFirstName_returnsMatchingStudents() {
        given(studentRepository.findAll(any(Specification.class))).willReturn(List.of(student));

        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName("John");

        List<StudentSummaryDto> result = studentService.findStudents(criteria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(0).lastName()).isEqualTo("Doe");
    }

    @Test
    void findStudents_noMatch_returnsEmptyList() {
        given(studentRepository.findAll(any(Specification.class))).willReturn(List.of());

        List<StudentSummaryDto> result = studentService.findStudents(new StudentSearchCriteria());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_exists_returnsStudentDetailDto() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        StudentDetailDto dto = studentService.findById(1L);

        assertThat(dto.firstName()).isEqualTo("John");
        assertThat(dto.lastName()).isEqualTo("Doe");
        assertThat(dto.sectionName()).isEqualTo("2024-2025");
    }

    @Test
    void findById_notFound_throwsObjectNotFoundException() {
        given(studentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_exists_deletesStudent() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void delete_notFound_throwsObjectNotFoundException() {
        given(studentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void removeFromTeam_studentOnTeam_removesSuccessfully() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        studentService.removeFromTeam(1L, 1L);

        assertThat(team.getStudents()).doesNotContain(student);
    }

    @Test
    void removeFromTeam_studentNotOnTeam_throwsIllegalStateException() {
        Team otherTeam = new Team();
        otherTeam.setId(2L);
        otherTeam.setName("Other Team");
        otherTeam.setStudents(new ArrayList<>());

        given(teamRepository.findById(2L)).willReturn(Optional.of(otherTeam));
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.removeFromTeam(2L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a member");
    }
}
