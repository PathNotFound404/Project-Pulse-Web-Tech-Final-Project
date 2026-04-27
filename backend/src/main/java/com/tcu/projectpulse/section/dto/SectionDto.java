package com.tcu.projectpulse.section.dto;

import java.time.LocalDate;
import java.util.List;

public record SectionDto(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String rubricName,
        List<String> teamNames
) {}