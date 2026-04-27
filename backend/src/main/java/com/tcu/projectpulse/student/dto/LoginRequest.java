package com.tcu.projectpulse.student.dto;

public record LoginRequest(
        String email,
        String password
) {}