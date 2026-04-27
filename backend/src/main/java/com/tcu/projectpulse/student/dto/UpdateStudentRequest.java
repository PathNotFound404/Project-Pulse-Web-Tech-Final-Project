package com.tcu.projectpulse.student.dto;

public record UpdateStudentRequest(
        String firstName,
        String lastName,
        String email
) {}