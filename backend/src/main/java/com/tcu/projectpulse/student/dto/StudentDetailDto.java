package com.tcu.projectpulse.student.dto;

import java.util.List;

public record StudentDetailDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String sectionName,
        List<String> teamNames,
        int warCount,
        int peerEvaluationCount
) {
}
