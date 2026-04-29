package com.tcu.projectpulse.auth.controller;

import com.tcu.projectpulse.auth.dto.LoginResponse;
import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.instructor.domain.Instructor;
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

    // UC-26: Student logs in
    @PostMapping("/login")
    Result login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            Long studentId = studentService.login(request.email(), request.password());
            session.setAttribute("studentId", studentId);
            UserProfileDto profile = studentService.getProfile(studentId);
            return Result.success(new LoginResponse(studentId, profile.firstName(), profile.lastName(), profile.email()));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
    }

    // UC-30: Instructor logs in
    @PostMapping("/login/instructor")
    Result loginInstructor(@RequestBody LoginRequest request, HttpSession session) {
        try {
            Instructor instructor = instructorService.login(request.email(), request.password());
            session.setAttribute("instructorId", instructor.getId());
            return Result.success(new LoginResponse(
                    instructor.getId(),
                    instructor.getFirstName(),
                    instructor.getLastName(),
                    instructor.getEmail()
            ));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
    }
}