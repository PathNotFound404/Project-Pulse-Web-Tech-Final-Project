package com.tcu.projectpulse.auth.controller;

import com.tcu.projectpulse.admin.domain.Admin;
import com.tcu.projectpulse.admin.repository.AdminRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final StudentService studentService;
    private final InstructorService instructorService;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(StudentService studentService,
                          InstructorService instructorService,
                          AdminRepository adminRepository) {
        this.studentService = studentService;
        this.instructorService = instructorService;
        this.adminRepository = adminRepository;
    }

    // UC-25: Student sets up a student account
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    Result registerStudent(@RequestParam String token, @RequestBody RegisterStudentRequest request) {
        studentService.register(token, request);
        return Result.success(Map.of("redirectTo", "/login"));
    }

    // UC-26: Unified login — tries student, then instructor, then admin
    @PostMapping("/login")
    Result login(@RequestBody LoginRequest request, HttpSession session) {
        // Try student
        try {
            Long studentId = studentService.login(request.email(), request.password());
            session.setAttribute("studentId", studentId);
            UserProfileDto profile = studentService.getProfile(studentId);
            return Result.success(new LoginResponse(studentId, profile.firstName(), profile.lastName(), profile.email(), "STUDENT"));
        } catch (IllegalArgumentException ignored) {}

        // Try instructor
        try {
            Long instructorId = instructorService.login(request.email(), request.password());
            session.setAttribute("instructorId", instructorId);
            InstructorDetailDto profile = instructorService.findById(instructorId);
            return Result.success(new LoginResponse(instructorId, profile.firstName(), profile.lastName(), profile.email(), "INSTRUCTOR"));
        } catch (IllegalArgumentException ignored) {}

        // Try admin
        Admin admin = adminRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");
        session.setAttribute("adminId", admin.getId());
        return Result.success(new LoginResponse(admin.getId(), admin.getFirstName(), admin.getLastName(), admin.getEmail(), "ADMIN"));
    }
}
