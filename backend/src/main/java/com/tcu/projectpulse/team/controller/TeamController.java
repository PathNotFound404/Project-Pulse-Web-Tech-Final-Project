package com.tcu.projectpulse.team.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.team.dto.AssignInstructorsRequest;
import com.tcu.projectpulse.team.dto.TeamSummaryDto;
import com.tcu.projectpulse.team.service.TeamService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // UC-14: Delete a senior design team
    @DeleteMapping("/{teamId}")
    Result deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return Result.success("Team deleted successfully", null);
    }

    // UC-19: Assign instructors to a team
    @PutMapping("/{teamId}/instructors")
    Result assignInstructors(@PathVariable Long teamId, @RequestBody AssignInstructorsRequest request) {
        TeamSummaryDto team = teamService.assignInstructors(teamId, request.instructorIds());
        return Result.success("Instructors assigned to team successfully", team);
    }

    // UC-20: Remove an instructor from a team
    @DeleteMapping("/{teamId}/instructors/{instructorId}")
    Result removeInstructor(@PathVariable Long teamId, @PathVariable Long instructorId) {
        TeamSummaryDto team = teamService.removeInstructor(teamId, instructorId);
        return Result.success("Instructor removed from team successfully", team);
    }
}
