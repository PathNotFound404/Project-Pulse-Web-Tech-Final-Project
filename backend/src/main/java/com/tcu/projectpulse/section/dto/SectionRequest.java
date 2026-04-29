package com.tcu.projectpulse.section.dto;

import java.time.LocalDate;

public record SectionRequest(String name, LocalDate startDate, LocalDate endDate, Long rubricId) {}