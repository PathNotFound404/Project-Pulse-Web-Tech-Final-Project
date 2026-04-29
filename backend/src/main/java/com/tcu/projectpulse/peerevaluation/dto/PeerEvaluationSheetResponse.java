package com.tcu.projectpulse.peerevaluation.dto;

import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationSheetResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<EvaluationEntryResponse> entries
) {}
