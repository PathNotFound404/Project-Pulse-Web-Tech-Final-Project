package com.tcu.projectpulse.instructor.dto;

// UC-30: Data needed to register an instructor account
public class InstructorRegistrationRequest {

    private String firstName;
    private String middleInitial;
    private String lastName;
    private String password;
    private String reenterPassword;

    public InstructorRegistrationRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getReenterPassword() { return reenterPassword; }
    public void setReenterPassword(String reenterPassword) { this.reenterPassword = reenterPassword; }
}