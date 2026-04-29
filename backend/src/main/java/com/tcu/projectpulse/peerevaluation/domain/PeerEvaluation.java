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
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id")
    private Student evaluatee;

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Column(name = "week_end")
    private LocalDate weekEnd;

    @Column
    private LocalDateTime submittedAt;

    @Column
    private Integer qualityOfWork;

    @Column
    private Integer productivity;

    @Column
    private Integer proactiveness;

    @Column
    private Integer treatsOthersWithRespect;

    @Column
    private Integer handlesCriticism;

    @Column
    private Integer performanceInMeetings;

    @Column(columnDefinition = "TEXT")
    private String publicComment;

    @Column(columnDefinition = "TEXT")
    private String privateComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id")
    private Student evaluator;

    @Column(name = "active_week")
    private String activeWeek;

    @Column
    private Integer initiative;

    @Column
    private Integer courtesy;

    @Column
    private Integer openMindedness;

    @Column
    private Integer engagementInMeetings;

    @Column(columnDefinition = "TEXT")
    private String publicComments;

    @Column(columnDefinition = "TEXT")
    private String privateComments;

    public PeerEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Student getEvaluatee() { return evaluatee; }
    public void setEvaluatee(Student evaluatee) { this.evaluatee = evaluatee; }

    public Student getEvaluator() { return evaluator; }
    public void setEvaluator(Student evaluator) { this.evaluator = evaluator; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getActiveWeek() { return activeWeek; }
    public void setActiveWeek(String activeWeek) { this.activeWeek = activeWeek; }

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

    public Integer getInitiative() { return initiative; }
    public void setInitiative(Integer initiative) { this.initiative = initiative; }

    public Integer getCourtesy() { return courtesy; }
    public void setCourtesy(Integer courtesy) { this.courtesy = courtesy; }

    public Integer getOpenMindedness() { return openMindedness; }
    public void setOpenMindedness(Integer openMindedness) { this.openMindedness = openMindedness; }

    public Integer getEngagementInMeetings() { return engagementInMeetings; }
    public void setEngagementInMeetings(Integer engagementInMeetings) { this.engagementInMeetings = engagementInMeetings; }

    public String getPublicComments() { return publicComments; }
    public void setPublicComments(String publicComments) { this.publicComments = publicComments; }

    public String getPrivateComments() { return privateComments; }
    public void setPrivateComments(String privateComments) { this.privateComments = privateComments; }

    public int getTotalScore() {
        int total = 0;
        if (qualityOfWork != null) total += qualityOfWork;
        if (productivity != null) total += productivity;
        if (initiative != null) total += initiative;
        if (courtesy != null) total += courtesy;
        if (openMindedness != null) total += openMindedness;
        if (engagementInMeetings != null) total += engagementInMeetings;
        return total;
    }

    public int getMaxScore() { return 60; }
}