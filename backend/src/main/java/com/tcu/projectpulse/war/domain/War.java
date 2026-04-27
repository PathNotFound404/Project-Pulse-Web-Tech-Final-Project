package com.tcu.projectpulse.war.domain;

import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wars")
public class War {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // e.g. "02-12-2024 to 02-18-2024"
    @Column(nullable = false)
    private String activeWeek;

    // DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, etc.
    @Column(nullable = false)
    private String activityCategory;

    @Column(nullable = false)
    private String plannedActivity;

    @Column
    private String description;

    @Column
    private Double plannedHours;

    @Column
    private Double actualHours;

    // "In progress", "Under testing", "Done"
    @Column
    private String status;

    public War() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getActiveWeek() { return activeWeek; }
    public void setActiveWeek(String activeWeek) { this.activeWeek = activeWeek; }

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
}