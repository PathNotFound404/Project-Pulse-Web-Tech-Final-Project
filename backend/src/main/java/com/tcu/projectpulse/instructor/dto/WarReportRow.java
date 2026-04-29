package com.tcu.projectpulse.instructor.dto;

public class WarReportRow {

    private String studentName;
    private String lastName;          // used for sorting by last name (spec requirement)
    private String weekStart;
    private String weekEnd;
    private String activityCategory;
    private String description;
    private double plannedHours;
    private double actualHours;
    private String status;
    private boolean submittedWar;

    public WarReportRow() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getWeekStart() { return weekStart; }
    public void setWeekStart(String weekStart) { this.weekStart = weekStart; }

    public String getWeekEnd() { return weekEnd; }
    public void setWeekEnd(String weekEnd) { this.weekEnd = weekEnd; }

    public String getActivityCategory() { return activityCategory; }
    public void setActivityCategory(String activityCategory) { this.activityCategory = activityCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPlannedHours() { return plannedHours; }
    public void setPlannedHours(double plannedHours) { this.plannedHours = plannedHours; }

    public double getActualHours() { return actualHours; }
    public void setActualHours(double actualHours) { this.actualHours = actualHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSubmittedWar() { return submittedWar; }
    public void setSubmittedWar(boolean submittedWar) { this.submittedWar = submittedWar; }
}