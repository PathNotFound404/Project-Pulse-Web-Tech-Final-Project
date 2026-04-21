package com.tcu.projectpulse.peerevaluation.domain;

import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "peer_evaluations")
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public PeerEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}
