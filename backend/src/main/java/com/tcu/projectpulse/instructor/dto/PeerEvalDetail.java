package com.tcu.projectpulse.instructor.dto;

public class PeerEvalDetail {

    private String evaluatorName;
    private Integer courtesy;
    private Integer engagementInMeetings;
    private Integer initiative;
    private Integer openMindedness;
    private Integer productivity;
    private Integer qualityOfWork;
    private int totalScore;
    private String publicComments;
    private String privateComments;

    public PeerEvalDetail() {}

    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }

    public Integer getCourtesy() { return courtesy; }
    public void setCourtesy(Integer courtesy) { this.courtesy = courtesy; }

    public Integer getEngagementInMeetings() { return engagementInMeetings; }
    public void setEngagementInMeetings(Integer engagementInMeetings) { this.engagementInMeetings = engagementInMeetings; }

    public Integer getInitiative() { return initiative; }
    public void setInitiative(Integer initiative) { this.initiative = initiative; }

    public Integer getOpenMindedness() { return openMindedness; }
    public void setOpenMindedness(Integer openMindedness) { this.openMindedness = openMindedness; }

    public Integer getProductivity() { return productivity; }
    public void setProductivity(Integer productivity) { this.productivity = productivity; }

    public Integer getQualityOfWork() { return qualityOfWork; }
    public void setQualityOfWork(Integer qualityOfWork) { this.qualityOfWork = qualityOfWork; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getPublicComments() { return publicComments; }
    public void setPublicComments(String publicComments) { this.publicComments = publicComments; }

    public String getPrivateComments() { return privateComments; }
    public void setPrivateComments(String privateComments) { this.privateComments = privateComments; }
}
