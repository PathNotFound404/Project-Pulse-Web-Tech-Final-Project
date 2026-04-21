package com.tcu.projectpulse.team;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.dto.TeamSummaryDto;
import com.tcu.projectpulse.team.repository.TeamRepository;
import com.tcu.projectpulse.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private TeamService teamService;

    private Section section;
    private Team team;
    private Student student;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        section = new Section("2024-2025");
        section.setId(1L);

        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@tcu.edu");
        student.setTeams(new ArrayList<>());

        instructor = new Instructor();
        instructor.setId(1L);
        instructor.setFirstName("Jane");
        instructor.setLastName("Smith");
        instructor.setEmail("j.smith@tcu.edu");
        instructor.setStatus(InstructorStatus.ACTIVE);
        instructor.setTeams(new ArrayList<>());

        team = new Team();
        team.setId(1L);
        team.setName("Team Alpha");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(student)));
        team.setInstructors(new ArrayList<>(List.of(instructor)));

        student.getTeams().add(team);
        instructor.getTeams().add(team);
    }

    @Test
    void deleteTeam_exists_deletesTeam() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        teamService.deleteTeam(1L);

        verify(teamRepository).delete(team);
        assertThat(team.getStudents()).isEmpty();
        assertThat(team.getInstructors()).isEmpty();
    }

    @Test
    void deleteTeam_notFound_throwsObjectNotFoundException() {
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void assignInstructors_validIds_associatesInstructors() {
        Instructor newInstructor = new Instructor();
        newInstructor.setId(2L);
        newInstructor.setFirstName("Bob");
        newInstructor.setLastName("Jones");
        newInstructor.setEmail("b.jones@tcu.edu");
        newInstructor.setStatus(InstructorStatus.ACTIVE);
        newInstructor.setTeams(new ArrayList<>());

        Team emptyTeam = new Team();
        emptyTeam.setId(2L);
        emptyTeam.setName("Team Beta");
        emptyTeam.setSection(section);
        emptyTeam.setStudents(new ArrayList<>());
        emptyTeam.setInstructors(new ArrayList<>());

        given(teamRepository.findById(2L)).willReturn(Optional.of(emptyTeam));
        given(instructorRepository.findAllById(List.of(2L))).willReturn(List.of(newInstructor));

        TeamSummaryDto result = teamService.assignInstructors(2L, List.of(2L));

        assertThat(emptyTeam.getInstructors()).contains(newInstructor);
        assertThat(result.instructorNames()).contains("Bob Jones");
    }

    @Test
    void assignInstructors_invalidIds_throwsIllegalArgumentException() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(instructorRepository.findAllById(List.of(99L))).willReturn(List.of());

        assertThatThrownBy(() -> teamService.assignInstructors(1L, List.of(99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void removeInstructor_instructorOnTeam_removes() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        TeamSummaryDto result = teamService.removeInstructor(1L, 1L);

        assertThat(team.getInstructors()).doesNotContain(instructor);
        assertThat(result.instructorNames()).isEmpty();
    }

    @Test
    void removeInstructor_notOnTeam_throwsIllegalStateException() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.removeInstructor(1L, 99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not assigned");
    }
}
