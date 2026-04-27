package com.tcu.projectpulse.student.dto;

public record RegisterStudentRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}