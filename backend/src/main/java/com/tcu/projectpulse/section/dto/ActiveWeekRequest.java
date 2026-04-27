package com.tcu.projectpulse.section.dto;

import java.time.LocalDate;
import java.util.List;

public record ActiveWeekRequest(List<LocalDate> inactiveWeekStarts) {}