package com.tcu.projectpulse.peerevaluation.dto;

import java.util.List;

public record SubmitPeerEvaluationsRequest(
        String week,
        List<EvaluationEntryRequest> entries
) {}
