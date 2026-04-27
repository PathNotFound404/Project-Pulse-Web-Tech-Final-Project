package com.tcu.projectpulse.war.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
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

import java.time.DayOfWeek;
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

    // UC-27: Get or create the WAR for the week containing the given date
    public WarResponse getOrCreateWar(Long studentId, LocalDate anyDateInWeek) {
        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
        if (weekStart.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot view a future week");
        }

        War war = warRepository.findByStudentIdAndWeekStart(studentId, weekStart)
                .orElseGet(() -> {
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));
                    War w = new War();
                    w.setStudent(student);
                    w.setWeekStart(weekStart);
                    w.setWeekEnd(weekStart.plusDays(6));
                    return warRepository.save(w);
                });

        return toWarResponse(war);
    }

    // UC-27: Add a new activity to a WAR
    public ActivityResponse addActivity(Long warId, Long studentId, ActivityRequest request) {
        War war = findWarOwnedByStudent(warId, studentId);
        validateActivityRequest(request);

        Activity activity = new Activity();
        activity.setWar(war);
        activity.setCategory(parseCategory(request.category()));
        activity.setDescription(request.description().trim());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        activity.setStatus(parseStatus(request.status()));

        return toActivityResponse(activityRepository.save(activity));
    }

    // UC-27: Edit an existing activity
    public ActivityResponse updateActivity(Long warId, Long activityId, Long studentId, ActivityRequest request) {
        findWarOwnedByStudent(warId, studentId);
        validateActivityRequest(request);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("Activity", activityId));
        if (!activity.getWar().getId().equals(warId)) {
            throw new IllegalArgumentException("Activity does not belong to this WAR");
        }

        activity.setCategory(parseCategory(request.category()));
        activity.setDescription(request.description().trim());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        activity.setStatus(parseStatus(request.status()));

        return toActivityResponse(activity);
    }

    // UC-27: Delete an activity
    public void deleteActivity(Long warId, Long activityId, Long studentId) {
        findWarOwnedByStudent(warId, studentId);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("Activity", activityId));
        if (!activity.getWar().getId().equals(warId)) {
            throw new IllegalArgumentException("Activity does not belong to this WAR");
        }

        activityRepository.delete(activity);
    }

    private War findWarOwnedByStudent(Long warId, Long studentId) {
        War war = warRepository.findById(warId)
                .orElseThrow(() -> new ObjectNotFoundException("WAR", warId));
        if (!war.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("WAR does not belong to this student");
        }
        return war;
    }

    private void validateActivityRequest(ActivityRequest request) {
        if (request.category() == null || request.category().isBlank())
            throw new IllegalArgumentException("Category is required");
        parseCategory(request.category());

        if (request.description() == null || request.description().isBlank())
            throw new IllegalArgumentException("Description is required");

        if (request.plannedHours() <= 0)
            throw new IllegalArgumentException("Planned hours must be a positive number");

        if (request.actualHours() < 0)
            throw new IllegalArgumentException("Actual hours cannot be negative");

        if (request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException("Status is required");
        parseStatus(request.status());
    }

    private ActivityCategory parseCategory(String value) {
        try {
            return ActivityCategory.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + value);
        }
    }

    private ActivityStatus parseStatus(String value) {
        try {
            return ActivityStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + value);
        }
    }

    private WarResponse toWarResponse(War war) {
        List<ActivityResponse> activities = war.getActivities().stream()
                .map(this::toActivityResponse)
                .toList();
        return new WarResponse(war.getId(), war.getWeekStart(), war.getWeekEnd(), activities);
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
