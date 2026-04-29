package com.tcu.projectpulse.peerevaluation.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.peerevaluation.dto.SubmitPeerEvaluationsRequest;
import com.tcu.projectpulse.peerevaluation.service.PeerEvaluationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/peer-evaluations")
public class PeerEvaluationController {

    private final PeerEvaluationService peerEvaluationService;

    public PeerEvaluationController(PeerEvaluationService peerEvaluationService) {
        this.peerEvaluationService = peerEvaluationService;
    }

    // UC-28: Get teammates and existing scores for a given week
    @GetMapping("/team")
    Result getSheet(@RequestParam String week, HttpSession session) {
        Long studentId = requireAuth(session);
        LocalDate date = LocalDate.parse(week);
        return Result.success(peerEvaluationService.getSheet(studentId, date));
    }

    // UC-29: View own peer evaluation report for a given week
    @GetMapping("/my-report")
    Result getMyReport(@RequestParam String week, HttpSession session) {
        Long studentId = requireAuth(session);
        LocalDate date = LocalDate.parse(week);
        return Result.success(peerEvaluationService.getMyReport(studentId, date));
    }

    // UC-28: Submit or update peer evaluations for the week
    @PostMapping
    Result submit(@RequestBody SubmitPeerEvaluationsRequest request, HttpSession session) {
        Long studentId = requireAuth(session);
        LocalDate weekStart = LocalDate.parse(request.week());
        return Result.success("Peer evaluation submitted successfully",
                peerEvaluationService.submit(studentId, weekStart, request.entries()));
    }

    private Long requireAuth(HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not authenticated");
        return studentId;
    }
}
