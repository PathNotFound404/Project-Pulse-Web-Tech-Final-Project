package com.tcu.projectpulse.instructor.dto;

import java.util.List;

public class PeerEvalReportRow {

    private String studentName;
    private String lastName;          // used for sorting by last name (spec requirement)
    private String week;              // used in UC-33 per-week breakdown
    private Double averageGrade;
    private Double maxGrade;
    private List<String> publicComments;
    private boolean submittedEval;

    public PeerEvalReportRow() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public Double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(Double averageGrade) { this.averageGrade = averageGrade; }

    public Double getMaxGrade() { return maxGrade; }
    public void setMaxGrade(Double maxGrade) { this.maxGrade = maxGrade; }

    public List<String> getPublicComments() { return publicComments; }
    public void setPublicComments(List<String> publicComments) { this.publicComments = publicComments; }

    public boolean isSubmittedEval() { return submittedEval; }
    public void setSubmittedEval(boolean submittedEval) { this.submittedEval = submittedEval; }
}