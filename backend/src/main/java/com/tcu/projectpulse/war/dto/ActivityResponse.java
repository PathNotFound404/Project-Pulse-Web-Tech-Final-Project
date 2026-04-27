package com.tcu.projectpulse.war.dto;

public record ActivityResponse(
        Long id,
        String category,
        String description,
        double plannedHours,
        double actualHours,
        String status
) {}
