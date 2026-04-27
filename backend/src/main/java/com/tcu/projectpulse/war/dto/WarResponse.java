package com.tcu.projectpulse.war.dto;

import java.util.List;

public record WarResponse(
        Long warId,
        String weekStart,
        String weekEnd,
        List<ActivityResponse> activities
) {}
