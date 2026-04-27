package com.tcu.projectpulse.war.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.war.dto.ActivityRequest;
import com.tcu.projectpulse.war.dto.ActivityResponse;
import com.tcu.projectpulse.war.dto.WarResponse;
import com.tcu.projectpulse.war.service.WarService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/wars")
public class WarController {

    private final WarService warService;

    public WarController(WarService warService) {
        this.warService = warService;
    }

    @GetMapping
    Result getWar(@RequestParam String week, HttpSession session) {
        Long studentId = requireStudentId(session);
        LocalDate weekStart = LocalDate.parse(week);
        WarResponse war = warService.getOrCreateWarForWeek(studentId, weekStart);
        return Result.success(war);
    }

    @PostMapping("/{warId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    Result addActivity(@PathVariable Long warId, @RequestBody ActivityRequest request, HttpSession session) {
        Long studentId = requireStudentId(session);
        ActivityResponse activity = warService.addActivity(studentId, warId, request);
        return Result.success("Activity added successfully", activity);
    }

    @PutMapping("/{warId}/activities/{activityId}")
    Result updateActivity(@PathVariable Long warId, @PathVariable Long activityId,
                          @RequestBody ActivityRequest request, HttpSession session) {
        Long studentId = requireStudentId(session);
        ActivityResponse activity = warService.updateActivity(studentId, warId, activityId, request);
        return Result.success("Activity updated successfully", activity);
    }

    @DeleteMapping("/{warId}/activities/{activityId}")
    Result deleteActivity(@PathVariable Long warId, @PathVariable Long activityId, HttpSession session) {
        Long studentId = requireStudentId(session);
        warService.deleteActivity(studentId, warId, activityId);
        return Result.success("Activity deleted successfully", null);
    }

    private Long requireStudentId(HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not logged in");
        return studentId;
    }
}
