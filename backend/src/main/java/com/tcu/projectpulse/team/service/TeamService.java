package com.tcu.projectpulse.team.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.team.domain.Team;
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

    public TeamService(TeamRepository teamRepository, InstructorRepository instructorRepository) {
        this.teamRepository = teamRepository;
        this.instructorRepository = instructorRepository;
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
}
