package com.tcu.projectpulse.war.dto;

public record ActivityRequest(
        String category,
        String description,
        Double plannedHours,
        Double actualHours,
        String status
) {}
