package com.tcu.projectpulse.instructor.dto;

import com.tcu.projectpulse.instructor.domain.InstructorStatus;

import java.util.Map;
import java.util.List;

public record InstructorDetailDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        InstructorStatus status,
        Map<String, List<String>> supervisedTeamsBySection
) {
}
