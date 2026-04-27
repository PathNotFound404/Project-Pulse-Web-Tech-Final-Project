package com.tcu.projectpulse.instructor.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.dto.*;
import com.tcu.projectpulse.instructor.service.InstructorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@CrossOrigin(origins = "http://localhost:5173")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    // UC-21: Find instructors
    @GetMapping
    public Result findInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status) {
        InstructorSearchCriteria criteria = new InstructorSearchCriteria();
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setTeamName(teamName);
        if (status != null) {
            criteria.setStatus(InstructorStatus.valueOf(status.toUpperCase()));
        }
        List<InstructorSummaryDto> instructors = instructorService.findInstructors(criteria);
        return Result.success("Success", instructors);
    }

    // UC-22: View an instructor
    @GetMapping("/{id}")
    public Result getInstructor(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.findById(id);
        return Result.success("Success", instructor);
    }

    
    // UC-18: Invite instructors
    @PostMapping("/invite")
    public Result inviteInstructors(@RequestBody InviteRequest request) {
        List<InviteLinkDto> links = instructorService.generateInviteLinks(request.emails());
        return Result.success("Invitations generated", links);
    }

    // UC-23: Deactivate instructor
    @PutMapping("/{id}/deactivate")
    public Result deactivate(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.deactivate(id);
        return Result.success("Instructor deactivated", instructor);
    }

    // UC-24: Reactivate instructor
    @PutMapping("/{id}/reactivate")
    public Result reactivate(@PathVariable Long id) {
        InstructorDetailDto instructor = instructorService.reactivate(id);
        return Result.success("Instructor reactivated", instructor);
    }
}