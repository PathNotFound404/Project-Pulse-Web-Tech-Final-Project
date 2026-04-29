package com.tcu.projectpulse.peerevaluation.dto;

public record EvaluationEntryResponse(
        Long evaluateeId,
        String firstName,
        String lastName,
        Integer qualityOfWork,
        Integer productivity,
        Integer proactiveness,
        Integer treatsOthersWithRespect,
        Integer handlesCriticism,
        Integer performanceInMeetings,
        String publicComment
        // privateComment intentionally omitted — instructor only
) {}
