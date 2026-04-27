package com.tcu.projectpulse.instructor.dto;

import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import java.util.List;

// Used to return instructor details in API responses
public class InstructorResponse {

    private Long id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String email;
    private InstructorStatus status;
    private List<String> teamNames;

    public InstructorResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public InstructorStatus getStatus() { return status; }
    public void setStatus(InstructorStatus status) { this.status = status; }

    public List<String> getTeamNames() { return teamNames; }
    public void setTeamNames(List<String> teamNames) { this.teamNames = teamNames; }
}