package com.tcu.projectpulse.team.domain;

import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String websiteUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_student",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_instructor",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private List<Instructor> instructors = new ArrayList<>();

    public Team() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }

    public List<Instructor> getInstructors() { return instructors; }
    public void setInstructors(List<Instructor> instructors) { this.instructors = instructors; }
}
