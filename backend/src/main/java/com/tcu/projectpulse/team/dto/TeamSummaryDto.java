package com.tcu.projectpulse.team.dto;

import java.util.List;

public record TeamSummaryDto(Long id, String name, String sectionName, List<String> studentNames, List<String> instructorNames) {
}
