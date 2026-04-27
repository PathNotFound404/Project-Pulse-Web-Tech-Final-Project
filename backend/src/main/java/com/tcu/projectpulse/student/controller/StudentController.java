package com.tcu.projectpulse.student.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.student.dto.*;
import com.tcu.projectpulse.student.service.StudentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // UC-15: Find students
    @GetMapping
    public Result findStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName) {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setEmail(email);
        criteria.setSectionName(sectionName);
        criteria.setTeamName(teamName);
        List<StudentSummaryDto> students = studentService.findStudents(criteria);
        return Result.success("Success", students);
    }

    // UC-16: View a student
    @GetMapping("/{id}")
    public Result getStudent(@PathVariable Long id) {
        StudentDetailDto student = studentService.findById(id);
        return Result.success("Success", student);
    }

    // UC-17: Delete student
    @DeleteMapping("/{id}")
    public Result deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success("Student deleted", null);
    }

    // UC-13: Remove student from team
    @DeleteMapping("/teams/{teamId}/students/{studentId}")
    public Result removeFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long studentId) {
        studentService.removeFromTeam(teamId, studentId);
        return Result.success("Removed from team", null);
    }
    // UC-11: Invite students to join a senior design section
    // Generates a unique registration link for each email provided
    // Does NOT send emails - returns the links to the admin to distribute
    @PostMapping("/invite")
    public Result inviteStudents(@RequestBody InviteRequest request) {
        List<InviteLinkDto> links = studentService.generateInviteLinks(request.emails());
        return Result.success("Invitations generated", links);
    }
}
