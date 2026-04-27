package com.tcu.projectpulse.peerevaluation.repository;

import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {

    // UC-31: Get all peer evals for a given week
    // Note: sectionId filtering done in service layer since Student->team relationship unknown
    @Query("SELECT pe FROM PeerEvaluation pe WHERE pe.activeWeek = :activeWeek")
    List<PeerEvaluation> findBySectionIdAndActiveWeek(@Param("sectionId") Long sectionId,
                                                       @Param("activeWeek") String activeWeek);

    // UC-33: Get all peer evals received by a student across a date range
    @Query("SELECT pe FROM PeerEvaluation pe WHERE pe.student.id = :studentId AND pe.activeWeek BETWEEN :startWeek AND :endWeek")
    List<PeerEvaluation> findByStudentIdAndWeekRange(@Param("studentId") Long studentId,
                                                      @Param("startWeek") String startWeek,
                                                      @Param("endWeek") String endWeek);

    // UC-31: Get all peer evals for a specific student in a given week
    @Query("SELECT pe FROM PeerEvaluation pe WHERE pe.student.id = :studentId AND pe.activeWeek = :activeWeek")
    List<PeerEvaluation> findByStudentIdAndActiveWeek(@Param("studentId") Long studentId,
                                                       @Param("activeWeek") String activeWeek);
}