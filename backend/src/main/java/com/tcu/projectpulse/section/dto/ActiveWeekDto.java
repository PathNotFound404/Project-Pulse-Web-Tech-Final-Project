package com.tcu.projectpulse.section.dto;

import java.time.LocalDate;

public record ActiveWeekDto(Long id, LocalDate startDate, LocalDate endDate, Boolean isActive) {}