package com.tcu.projectpulse.peerevaluation.dto;

import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationReportResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        String firstName,
        String lastName,
        Double avgQualityOfWork,
        Double avgProductivity,
        Double avgProactiveness,
        Double avgTreatsOthersWithRespect,
        Double avgHandlesCriticism,
        Double avgPerformanceInMeetings,
        List<String> publicComments,
        Double grade
) {}
