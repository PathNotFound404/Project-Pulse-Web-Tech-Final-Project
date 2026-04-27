package com.tcu.projectpulse.war.dto;

import java.time.LocalDate;
import java.util.List;

public record WarResponse(
        Long id,
        LocalDate weekStart,
        LocalDate weekEnd,
        List<ActivityResponse> activities
) {}
