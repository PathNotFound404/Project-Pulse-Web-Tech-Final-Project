package com.tcu.projectpulse.peerevaluation.domain;

import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "peer_evaluations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "evaluatee_id", "week_start"}))
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // the evaluator

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id", nullable = false)
    private Student evaluatee;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(nullable = false)
    private Integer qualityOfWork;

    @Column(nullable = false)
    private Integer productivity;

    @Column(nullable = false)
    private Integer proactiveness;

    @Column(nullable = false)
    private Integer treatsOthersWithRespect;

    @Column(nullable = false)
    private Integer handlesCriticism;

    @Column(nullable = false)
    private Integer performanceInMeetings;

    @Column(columnDefinition = "TEXT")
    private String publicComment;

    @Column(columnDefinition = "TEXT")
    private String privateComment;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public PeerEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Student getEvaluatee() { return evaluatee; }
    public void setEvaluatee(Student evaluatee) { this.evaluatee = evaluatee; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public Integer getQualityOfWork() { return qualityOfWork; }
    public void setQualityOfWork(Integer qualityOfWork) { this.qualityOfWork = qualityOfWork; }

    public Integer getProductivity() { return productivity; }
    public void setProductivity(Integer productivity) { this.productivity = productivity; }

    public Integer getProactiveness() { return proactiveness; }
    public void setProactiveness(Integer proactiveness) { this.proactiveness = proactiveness; }

    public Integer getTreatsOthersWithRespect() { return treatsOthersWithRespect; }
    public void setTreatsOthersWithRespect(Integer treatsOthersWithRespect) { this.treatsOthersWithRespect = treatsOthersWithRespect; }

    public Integer getHandlesCriticism() { return handlesCriticism; }
    public void setHandlesCriticism(Integer handlesCriticism) { this.handlesCriticism = handlesCriticism; }

    public Integer getPerformanceInMeetings() { return performanceInMeetings; }
    public void setPerformanceInMeetings(Integer performanceInMeetings) { this.performanceInMeetings = performanceInMeetings; }

    public String getPublicComment() { return publicComment; }
    public void setPublicComment(String publicComment) { this.publicComment = publicComment; }

    public String getPrivateComment() { return privateComment; }
    public void setPrivateComment(String privateComment) { this.privateComment = privateComment; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
