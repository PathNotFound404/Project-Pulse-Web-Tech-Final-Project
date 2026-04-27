package com.tcu.projectpulse.rubric.dto;

import java.util.List;

public record RubricRequest(String name, List<RubricCriterionDto> criteria) {}