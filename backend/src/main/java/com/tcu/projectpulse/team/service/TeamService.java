package com.tcu.projectpulse.team.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.dto.TeamRequest;
import com.tcu.projectpulse.team.dto.TeamSummaryDto;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final InstructorRepository instructorRepository;
    private final com.tcu.projectpulse.student.repository.StudentRepository studentRepository;

    public TeamService(TeamRepository teamRepository, InstructorRepository instructorRepository,
                   com.tcu.projectpulse.student.repository.StudentRepository studentRepository) {
        this.teamRepository = teamRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
    }

    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));

        // Clear join table relationships before deleting
        team.getStudents().clear();
        team.getInstructors().clear();

        teamRepository.delete(team);
    }

    public TeamSummaryDto removeStudent(Long teamId, Long studentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));

        boolean removed = team.getStudents().removeIf(s -> s.getId().equals(studentId));
        if (!removed) {
            throw new IllegalStateException("Student " + studentId + " is not a member of team " + teamId);
        }

        return toSummaryDto(team);
    }

    public TeamSummaryDto assignInstructors(Long teamId, List<Long> instructorIds) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));

        List<Instructor> instructors = instructorRepository.findAllById(instructorIds);
        if (instructors.size() != instructorIds.size()) {
            throw new IllegalArgumentException("One or more instructor IDs not found");
        }

        for (Instructor instructor : instructors) {
            if (!team.getInstructors().contains(instructor)) {
                team.getInstructors().add(instructor);
            }
        }

        return toSummaryDto(team);
    }

    public TeamSummaryDto removeInstructor(Long teamId, Long instructorId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));

        boolean removed = team.getInstructors().removeIf(i -> i.getId().equals(instructorId));
        if (!removed) {
            throw new IllegalStateException("Instructor " + instructorId + " is not assigned to team " + teamId);
        }

        return toSummaryDto(team);
    }

    private TeamSummaryDto toSummaryDto(Team team) {
        String sectionName = team.getSection() != null ? team.getSection().getName() : null;
        List<String> studentNames = team.getStudents().stream()
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();
        List<String> instructorNames = team.getInstructors().stream()
                .map(i -> i.getFirstName() + " " + i.getLastName())
                .toList();
        return new TeamSummaryDto(team.getId(), team.getName(), sectionName, studentNames, instructorNames);
    }
    
    public List<TeamSummaryDto> findTeams(Long sectionId, String name, String instructor) {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .filter(t -> sectionId == null || (t.getSection() != null && t.getSection().getId().equals(sectionId)))
                .filter(t -> name == null || t.getName().toLowerCase().contains(name.toLowerCase()))
                .map(this::toSummaryDto)
                .toList();
    }

    public TeamSummaryDto findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Team", id));
        return toSummaryDto(team);
    }

    public TeamSummaryDto createTeam(TeamRequest request) {
        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());
        return toSummaryDto(teamRepository.save(team));
    }

    public TeamSummaryDto updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Team", id));
        if (request.name() != null) team.setName(request.name());
        if (request.description() != null) team.setDescription(request.description());
        if (request.websiteUrl() != null) team.setWebsiteUrl(request.websiteUrl());
        return toSummaryDto(teamRepository.save(team));
    }

    public TeamSummaryDto assignStudent(Long teamId, Long studentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));
        com.tcu.projectpulse.student.domain.Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));
        if (!team.getStudents().contains(student)) {
            team.getStudents().add(student);
        }
        return toSummaryDto(teamRepository.save(team));
    }
}
