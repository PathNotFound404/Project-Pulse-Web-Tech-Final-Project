package com.tcu.projectpulse.war.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.war.dto.ActivityRequest;
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

    // UC-27: Get (or create) the WAR for a given week
    @GetMapping
    Result getWar(@RequestParam String week, HttpSession session) {
        Long studentId = requireAuth(session);
        LocalDate date = LocalDate.parse(week);
        return Result.success(warService.getOrCreateWar(studentId, date));
    }

    // UC-27: Add a new activity to a WAR
    @PostMapping("/{warId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    Result addActivity(@PathVariable Long warId,
                       @RequestBody ActivityRequest request,
                       HttpSession session) {
        Long studentId = requireAuth(session);
        return Result.success(warService.addActivity(warId, studentId, request));
    }

    // UC-27: Edit an existing activity
    @PutMapping("/{warId}/activities/{activityId}")
    Result updateActivity(@PathVariable Long warId,
                          @PathVariable Long activityId,
                          @RequestBody ActivityRequest request,
                          HttpSession session) {
        Long studentId = requireAuth(session);
        return Result.success(warService.updateActivity(warId, activityId, studentId, request));
    }

    // UC-27: Delete an activity
    @DeleteMapping("/{warId}/activities/{activityId}")
    Result deleteActivity(@PathVariable Long warId,
                          @PathVariable Long activityId,
                          HttpSession session) {
        System.out.println("[DEBUG] DELETE /api/wars/" + warId + "/activities/" + activityId + " called");
        Long studentId = requireAuth(session);
        warService.deleteActivity(warId, activityId, studentId);
        return Result.success("Activity deleted successfully", null);
    }

    private Long requireAuth(HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) throw new UnauthorizedException("Not authenticated");
        return studentId;
    }
}
