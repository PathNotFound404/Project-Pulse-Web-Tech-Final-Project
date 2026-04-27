package com.tcu.projectpulse.war.dto;

public record ActivityRequest(
        String category,
        String description,
        double plannedHours,
        double actualHours,
        String status
) {}
