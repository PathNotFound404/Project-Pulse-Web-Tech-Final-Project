package com.tcu.projectpulse.auth.controller;

import com.tcu.projectpulse.auth.dto.LoginResponse;
import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.instructor.dto.InstructorDetailDto;
import com.tcu.projectpulse.instructor.service.InstructorService;
import com.tcu.projectpulse.student.dto.LoginRequest;
import com.tcu.projectpulse.student.dto.RegisterStudentRequest;
import com.tcu.projectpulse.student.dto.UserProfileDto;
import com.tcu.projectpulse.student.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentService studentService;
    private final InstructorService instructorService;

    public AuthController(StudentService studentService, InstructorService instructorService) {
        this.studentService = studentService;
        this.instructorService = instructorService;
    }

    // UC-25: Student sets up a student account
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    Result registerStudent(@RequestParam String token, @RequestBody RegisterStudentRequest request) {
        studentService.register(token, request);
        return Result.success(Map.of("redirectTo", "/login"));
    }

    // UC-26: Unified login — tries student first, then instructor
    @PostMapping("/login")
    Result login(@RequestBody LoginRequest request, HttpSession session) {
        // Try student
        try {
            Long studentId = studentService.login(request.email(), request.password());
            session.setAttribute("studentId", studentId);
            UserProfileDto profile = studentService.getProfile(studentId);
            return Result.success(new LoginResponse(studentId, profile.firstName(), profile.lastName(), profile.email()));
        } catch (IllegalArgumentException ignored) {}

        // Fall through to instructor
        try {
            Long instructorId = instructorService.login(request.email(), request.password());
            session.setAttribute("instructorId", instructorId);
            InstructorDetailDto profile = instructorService.findById(instructorId);
            return Result.success(new LoginResponse(instructorId, profile.firstName(), profile.lastName(), profile.email()));
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}
