package com.tcu.projectpulse.instructor.dto;

// UC-32 & UC-34: One row in a WAR report
public class WarReportRow {

    private String studentName;
    private String week;            // used in UC-34 (per-week breakdown)
    private String activityCategory;
    private String plannedActivity;
    private String description;
    private Double plannedHours;
    private Double actualHours;
    private String status;
    private boolean submittedWar;   // false = did not turn in WAR that week

    public WarReportRow() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public String getActivityCategory() { return activityCategory; }
    public void setActivityCategory(String activityCategory) { this.activityCategory = activityCategory; }

    public String getPlannedActivity() { return plannedActivity; }
    public void setPlannedActivity(String plannedActivity) { this.plannedActivity = plannedActivity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPlannedHours() { return plannedHours; }
    public void setPlannedHours(Double plannedHours) { this.plannedHours = plannedHours; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSubmittedWar() { return submittedWar; }
    public void setSubmittedWar(boolean submittedWar) { this.submittedWar = submittedWar; }
}