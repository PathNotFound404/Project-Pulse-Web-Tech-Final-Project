package com.tcu.projectpulse.student.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.instructor.dto.InstructorDetailDto;
import com.tcu.projectpulse.instructor.service.InstructorService;
import com.tcu.projectpulse.student.dto.UpdateStudentRequest;
import com.tcu.projectpulse.student.dto.UserProfileDto;
import com.tcu.projectpulse.student.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final StudentService studentService;
    private final InstructorService instructorService;

    public UserController(StudentService studentService, InstructorService instructorService) {
        this.studentService = studentService;
        this.instructorService = instructorService;
    }

    // UC-26: Get logged-in user's profile (student or instructor)
    @GetMapping("/me")
    Result getMe(HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId != null)
            return Result.success(studentService.getProfile(studentId));

        Long instructorId = (Long) session.getAttribute("instructorId");
        if (instructorId != null) {
            InstructorDetailDto i = instructorService.findById(instructorId);
            return Result.success(new UserProfileDto(i.firstName(), i.lastName(), i.email()));
        }

        throw new UnauthorizedException("Not authenticated");
    }

    // UC-26: Update logged-in student's profile
    @PutMapping("/me")
    Result updateMe(@RequestBody UpdateStudentRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not authenticated");
        return Result.success("Account updated successfully", studentService.update(studentId, request));
    }
}
