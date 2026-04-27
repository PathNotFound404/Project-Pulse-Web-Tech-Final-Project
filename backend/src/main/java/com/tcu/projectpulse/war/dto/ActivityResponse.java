package com.tcu.projectpulse.war.dto;

public record ActivityResponse(
        Long id,
        String category,
        String description,
        Double plannedHours,
        Double actualHours,
        String status
) {}
