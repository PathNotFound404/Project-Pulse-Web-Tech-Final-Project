package com.tcu.projectpulse.student.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.student.dto.UpdateStudentRequest;
import com.tcu.projectpulse.student.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final StudentService studentService;

    public UserController(StudentService studentService) {
        this.studentService = studentService;
    }

    // UC-26: Get logged-in student's profile
    @GetMapping("/me")
    Result getMe(HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not authenticated");
        return Result.success(studentService.getProfile(studentId));
    }

    // UC-26: Update logged-in student's profile
    @PutMapping("/me")
    Result updateMe(@RequestBody UpdateStudentRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not authenticated");
        return Result.success("Account updated successfully", studentService.update(studentId, request));
    }
}
