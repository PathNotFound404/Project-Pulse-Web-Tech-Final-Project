package com.tcu.projectpulse.student.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.student.dto.StudentDetailDto;
import com.tcu.projectpulse.student.dto.StudentSearchCriteria;
import com.tcu.projectpulse.student.dto.StudentSummaryDto;
import com.tcu.projectpulse.student.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // UC-15: Find students
    @GetMapping("/students")
    Result findStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teamId) {

        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setEmail(email);
        criteria.setSectionName(sectionName);
        criteria.setTeamName(teamName);
        criteria.setSectionId(sectionId);
        criteria.setTeamId(teamId);

        List<StudentSummaryDto> students = studentService.findStudents(criteria);
        return Result.success(students);
    }

    // UC-16: View a student
    @GetMapping("/students/{id}")
    Result getStudent(@PathVariable Long id) {
        StudentDetailDto student = studentService.findById(id);
        return Result.success(student);
    }

    // UC-17: Delete a student
    @DeleteMapping("/students/{id}")
    Result deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success("Student deleted successfully", null);
    }

    // UC-13: Remove a student from a team
    @DeleteMapping("/teams/{teamId}/students/{studentId}")
    Result removeStudentFromTeam(@PathVariable Long teamId, @PathVariable Long studentId) {
        studentService.removeFromTeam(teamId, studentId);
        return Result.success("Student removed from team successfully", null);
    }
}
