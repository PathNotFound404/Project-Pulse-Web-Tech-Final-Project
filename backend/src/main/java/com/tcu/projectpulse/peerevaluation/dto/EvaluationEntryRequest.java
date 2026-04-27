package com.tcu.projectpulse.peerevaluation.dto;

public record EvaluationEntryRequest(
        Long evaluateeId,
        Integer qualityOfWork,
        Integer productivity,
        Integer proactiveness,
        Integer treatsOthersWithRespect,
        Integer handlesCriticism,
        Integer performanceInMeetings,
        String publicComment,
        String privateComment
) {}
