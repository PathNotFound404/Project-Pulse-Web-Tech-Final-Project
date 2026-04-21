package com.tcu.projectpulse.instructor.dto;

import com.tcu.projectpulse.instructor.domain.InstructorStatus;

import java.util.List;

public record InstructorSummaryDto(Long id, String firstName, String lastName, List<String> teamNames, InstructorStatus status) {
}
