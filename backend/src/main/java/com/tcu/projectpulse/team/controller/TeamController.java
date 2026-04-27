package com.tcu.projectpulse.team.controller;

import java.util.List;
import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.team.dto.AssignInstructorsRequest;
import com.tcu.projectpulse.team.dto.TeamRequest;
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
    // UC-7: Find senior design teams
    @GetMapping
    public Result findTeams(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String instructor) {
        List<TeamSummaryDto> teams = teamService.findTeams(sectionId, name, instructor);
        return Result.success("Success", teams);
    }

    // UC-8: View a senior design team
    @GetMapping("/{teamId}")
    public Result getTeam(@PathVariable Long teamId) {
        TeamSummaryDto team = teamService.findById(teamId);
        return Result.success("Success", team);
    }

    // UC-9: Create a senior design team
    @PostMapping
    public Result createTeam(@RequestBody TeamRequest request) {
        TeamSummaryDto team = teamService.createTeam(request);
        return Result.success("Team created", team);
    }

    // UC-10: Edit a senior design team
    @PutMapping("/{teamId}")
    public Result updateTeam(@PathVariable Long teamId, @RequestBody TeamRequest request) {
        TeamSummaryDto team = teamService.updateTeam(teamId, request);
        return Result.success("Team updated", team);
    }

    // UC-12: Assign student to team
    @PostMapping("/{teamId}/students/{studentId}")
    public Result assignStudent(@PathVariable Long teamId,
                                 @PathVariable Long studentId) {
        TeamSummaryDto team = teamService.assignStudent(teamId, studentId);
        return Result.success("Student assigned", team);
    }

    // UC-13: Remove student from team
    @DeleteMapping("/{teamId}/students/{studentId}")
    public Result removeStudent(@PathVariable Long teamId,
                                 @PathVariable Long studentId) {
        TeamSummaryDto team = teamService.removeStudent(teamId, studentId);
        return Result.success("Student removed", team);
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
