package com.tcu.projectpulse.rubric.dto;

import java.util.List;

public record RubricDto(Long id, String name, List<RubricCriterionDto> criteria) {}