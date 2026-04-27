package com.tcu.projectpulse.instructor.dto;

import java.util.List;

// UC-31 & UC-33: Peer Evaluation Report row (one row per student)
public class PeerEvalReportRow {

    private String studentName;
    private String week;            // used in UC-33 (per-week breakdown)
    private Double averageGrade;        // e.g. 54.0 out of 60
    private Double maxGrade;            // e.g. 60.0
    private List<String> publicComments;
    private boolean submittedEval;      // false = did not turn in peer eval that week

    public PeerEvalReportRow() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

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