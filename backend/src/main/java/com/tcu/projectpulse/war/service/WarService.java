package com.tcu.projectpulse.war.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.common.exception.UnauthorizedException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.war.domain.Activity;
import com.tcu.projectpulse.war.domain.ActivityCategory;
import com.tcu.projectpulse.war.domain.ActivityStatus;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.dto.ActivityRequest;
import com.tcu.projectpulse.war.dto.ActivityResponse;
import com.tcu.projectpulse.war.dto.WarResponse;
import com.tcu.projectpulse.war.repository.ActivityRepository;
import com.tcu.projectpulse.war.repository.WarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WarService {

    private final WarRepository warRepository;
    private final ActivityRepository activityRepository;
    private final StudentRepository studentRepository;

    public WarService(WarRepository warRepository,
                      ActivityRepository activityRepository,
                      StudentRepository studentRepository) {
        this.warRepository = warRepository;
        this.activityRepository = activityRepository;
        this.studentRepository = studentRepository;
    }

    public WarResponse getOrCreateWarForWeek(Long studentId, LocalDate weekStart) {
        if (weekStart.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot access a future week");
        }

        LocalDate weekEnd = weekStart.plusDays(6);

        War war = warRepository.findByStudentIdAndWeekStart(studentId, weekStart)
                .orElseGet(() -> {
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));
                    War newWar = new War();
                    newWar.setStudent(student);
                    newWar.setWeekStart(weekStart);
                    newWar.setWeekEnd(weekEnd);
                    return warRepository.save(newWar);
                });

        return toWarResponse(war);
    }

    public ActivityResponse addActivity(Long studentId, Long warId, ActivityRequest request) {
        War war = getWarOwnedByStudent(studentId, warId);
        validateActivityRequest(request);

        Activity activity = new Activity();
        activity.setWar(war);
        activity.setCategory(ActivityCategory.valueOf(request.category()));
        activity.setDescription(request.description().trim());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        activity.setStatus(ActivityStatus.valueOf(request.status()));

        activity = activityRepository.save(activity);
        return toActivityResponse(activity);
    }

    public ActivityResponse updateActivity(Long studentId, Long warId, Long activityId, ActivityRequest request) {
        War war = getWarOwnedByStudent(studentId, warId);
        validateActivityRequest(request);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("Activity", activityId));

        if (!activity.getWar().getId().equals(war.getId())) {
            throw new IllegalArgumentException("Activity does not belong to this WAR");
        }

        activity.setCategory(ActivityCategory.valueOf(request.category()));
        activity.setDescription(request.description().trim());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        activity.setStatus(ActivityStatus.valueOf(request.status()));

        return toActivityResponse(activity);
    }

    public void deleteActivity(Long studentId, Long warId, Long activityId) {
        War war = getWarOwnedByStudent(studentId, warId);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("Activity", activityId));

        if (!activity.getWar().getId().equals(war.getId())) {
            throw new IllegalArgumentException("Activity does not belong to this WAR");
        }

        activityRepository.delete(activity);
    }

    private War getWarOwnedByStudent(Long studentId, Long warId) {
        War war = warRepository.findById(warId)
                .orElseThrow(() -> new ObjectNotFoundException("WAR", warId));

        if (!war.getStudent().getId().equals(studentId)) {
            throw new UnauthorizedException("You do not have access to this WAR");
        }

        return war;
    }

    private void validateActivityRequest(ActivityRequest request) {
        if (request.category() == null || request.category().isBlank())
            throw new IllegalArgumentException("Category is required");
        if (request.description() == null || request.description().isBlank())
            throw new IllegalArgumentException("Description is required");
        if (request.plannedHours() == null || request.plannedHours() <= 0)
            throw new IllegalArgumentException("Planned hours must be a positive number");
        if (request.actualHours() == null || request.actualHours() < 0)
            throw new IllegalArgumentException("Actual hours must be a non-negative number");
        if (request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException("Status is required");

        try {
            ActivityCategory.valueOf(request.category());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + request.category());
        }

        try {
            ActivityStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.status());
        }
    }

    private WarResponse toWarResponse(War war) {
        List<ActivityResponse> activities = war.getActivities().stream()
                .map(this::toActivityResponse)
                .toList();
        return new WarResponse(
                war.getId(),
                war.getWeekStart().toString(),
                war.getWeekEnd().toString(),
                activities
        );
    }

    private ActivityResponse toActivityResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getCategory().name(),
                activity.getDescription(),
                activity.getPlannedHours(),
                activity.getActualHours(),
                activity.getStatus().name()
        );
    }
}
