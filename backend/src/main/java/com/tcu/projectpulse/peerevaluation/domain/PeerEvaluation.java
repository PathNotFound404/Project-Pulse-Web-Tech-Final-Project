package com.tcu.projectpulse.peerevaluation.domain;

import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "peer_evaluations")
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The student being evaluated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // The student who submitted this evaluation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Student evaluator;

    // e.g. "02-12-2024 to 02-18-2024"
    @Column(nullable = false)
    private String activeWeek;

    // Individual rubric criterion scores (integers per UC-28)
    @Column
    private Integer qualityOfWork;

    @Column
    private Integer productivity;

    @Column
    private Integer initiative;

    @Column
    private Integer courtesy;

    @Column
    private Integer openMindedness;

    @Column
    private Integer engagementInMeetings;

    // Public comments — visible to the student being evaluated
    @Column(columnDefinition = "TEXT")
    private String publicComments;

    // Private comments — visible to instructor only (UC-31, BR-5)
    @Column(columnDefinition = "TEXT")
    private String privateComments;

    public PeerEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Student getEvaluator() { return evaluator; }
    public void setEvaluator(Student evaluator) { this.evaluator = evaluator; }

    public String getActiveWeek() { return activeWeek; }
    public void setActiveWeek(String activeWeek) { this.activeWeek = activeWeek; }

    public Integer getQualityOfWork() { return qualityOfWork; }
    public void setQualityOfWork(Integer qualityOfWork) { this.qualityOfWork = qualityOfWork; }

    public Integer getProductivity() { return productivity; }
    public void setProductivity(Integer productivity) { this.productivity = productivity; }

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

    // Helper: compute total score for this evaluation (sum of all criteria)
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

    // Max possible score = 6 criteria x 10 = 60
    public int getMaxScore() {
        return 60;
    }
}