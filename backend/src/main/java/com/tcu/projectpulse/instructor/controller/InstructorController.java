package com.tcu.projectpulse.instructor.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.dto.*;
import com.tcu.projectpulse.instructor.service.InstructorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    // UC-21: Find instructors
    @GetMapping
    Result findInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) InstructorStatus status) {
        InstructorSearchCriteria criteria = new InstructorSearchCriteria();
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setTeamName(teamName);
        criteria.setStatus(status);
        List<InstructorSummaryDto> instructors = instructorService.findInstructors(criteria);
        return Result.success(instructors);
    }

    // UC-22: View an instructor
    @GetMapping("/{id}")
    Result getInstructor(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.findById(id);
        return Result.success(instructor);
    }

    // UC-18: Invite instructors to register an account
    @PostMapping("/invite")
    Result inviteInstructors(@RequestBody InviteRequest request) {
        if (request.emails() == null || request.emails().isEmpty()) {
            throw new IllegalArgumentException("At least one email is required");
        }
        List<InviteLinkDto> links = instructorService.generateInviteLinks(request.emails());
        return Result.success("Invitation links generated. Share these links with the instructors.", links);
    }

    // UC-23: Deactivate an instructor
    @PutMapping("/{id}/deactivate")
    Result deactivateInstructor(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.deactivate(id);
        return Result.success("Instructor deactivated successfully", instructor);
    }

    // UC-24: Reactivate an instructor
    @PutMapping("/{id}/reactivate")
    Result reactivateInstructor(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.reactivate(id);
        return Result.success("Instructor reactivated successfully", instructor);
    }

    // UC-30: Instructor sets up account via invite token
    @PostMapping("/register")
    public Result register(@RequestParam String token,
                           @RequestBody InstructorRegistrationRequest request) {
        return Result.success("Instructor account created successfully",
                instructorService.registerInstructor(token, request));
    }

    // UC-31: Generate peer eval report for entire senior design section
    @GetMapping("/reports/peer-evaluation/section/{sectionId}")
    public Result getPeerEvalReportForSection(@PathVariable Long sectionId,
                                              @RequestParam String activeWeek) {
        return Result.success("Peer evaluation report generated",
                instructorService.generateSectionPeerEvalReport(sectionId, activeWeek));
    }

    // UC-32: Generate WAR report for a team
    @GetMapping("/reports/war/team/{teamId}")
    public Result getWarReportForTeam(@PathVariable Long teamId,
                                      @RequestParam String activeWeek) {
        return Result.success("WAR report generated",
                instructorService.generateTeamWarReport(teamId, activeWeek));
    }

    // UC-33: Generate peer eval report for a specific student
    @GetMapping("/reports/peer-evaluation/student/{studentId}")
    public Result getPeerEvalReportForStudent(@PathVariable Long studentId,
                                              @RequestParam String startWeek,
                                              @RequestParam String endWeek) {
        return Result.success("Student peer evaluation report generated",
                instructorService.generateStudentPeerEvalReport(studentId, startWeek, endWeek));
    }

    // UC-34: Generate WAR report for a specific student
    @GetMapping("/reports/war/student/{studentId}")
    public Result getWarReportForStudent(@PathVariable Long studentId,
                                         @RequestParam String startWeek,
                                         @RequestParam String endWeek) {
        return Result.success("Student WAR report generated",
                instructorService.generateStudentWarReport(studentId, startWeek, endWeek));
    }
}