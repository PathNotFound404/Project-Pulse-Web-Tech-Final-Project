package com.tcu.projectpulse.peerevaluation.repository;

import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {

    List<PeerEvaluation> findByStudentIdAndWeekStart(Long studentId, LocalDate weekStart);

    Optional<PeerEvaluation> findByStudentIdAndEvaluateeIdAndWeekStart(
            Long studentId, Long evaluateeId, LocalDate weekStart);
}
