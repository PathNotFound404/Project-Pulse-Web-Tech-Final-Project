package com.tcu.projectpulse.instructor.dto;

import com.tcu.projectpulse.instructor.domain.InstructorStatus;

public class InstructorSearchCriteria {

    private String firstName;
    private String lastName;
    private String teamName;
    private InstructorStatus status;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public InstructorStatus getStatus() { return status; }
    public void setStatus(InstructorStatus status) { this.status = status; }
}
