package com.tcu.projectpulse.section.domain;

import com.tcu.projectpulse.rubric.domain.Rubric;
import com.tcu.projectpulse.team.domain.Team;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActiveWeek> activeWeeks = new ArrayList<>();

    public Section() {}

    public Section(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Rubric getRubric() { return rubric; }
    public void setRubric(Rubric rubric) { this.rubric = rubric; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    public List<ActiveWeek> getActiveWeeks() { return activeWeeks; }
    public void setActiveWeeks(List<ActiveWeek> activeWeeks) { this.activeWeeks = activeWeeks; }
}