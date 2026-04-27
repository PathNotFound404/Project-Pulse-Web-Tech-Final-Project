package com.tcu.projectpulse.auth.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.student.dto.RegisterStudentRequest;
import com.tcu.projectpulse.student.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentService studentService;

    public AuthController(StudentService studentService) {
        this.studentService = studentService;
    }

    // UC-25: Student sets up a student account
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    Result registerStudent(@RequestParam String token, @RequestBody RegisterStudentRequest request) {
        studentService.register(token, request);
        return Result.success(Map.of("redirectTo", "/login"));
    }
}