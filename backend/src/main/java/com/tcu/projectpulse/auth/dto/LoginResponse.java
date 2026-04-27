package com.tcu.projectpulse.auth.dto;

public record LoginResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {}
