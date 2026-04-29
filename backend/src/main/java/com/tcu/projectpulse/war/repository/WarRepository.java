package com.tcu.projectpulse.war.repository;

import com.tcu.projectpulse.war.domain.War;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarRepository extends JpaRepository<War, Long> {

    // UC-27 (Cody): get a student's WAR for a specific week start
    Optional<War> findByStudentIdAndWeekStart(Long studentId, LocalDate weekStart);

    // UC-32: All WARs for a given team whose week_start matches the requested date.
    // Team membership is resolved via Student -> teams join.
    @Query("""
           SELECT w FROM War w
           JOIN w.student s
           JOIN s.teams t
           WHERE t.id = :teamId
             AND w.weekStart = :weekStart
           """)
    List<War> findByTeamIdAndWeekStart(@Param("teamId") Long teamId,
                                       @Param("weekStart") LocalDate weekStart);

    // UC-34: All WARs for a student whose week_start falls within [start, end].
    @Query("""
           SELECT w FROM War w
           WHERE w.student.id = :studentId
             AND w.weekStart BETWEEN :startDate AND :endDate
           """)
    List<War> findByStudentIdAndWeekRange(@Param("studentId") Long studentId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // UC-32: IDs of students who already submitted a WAR for the given team/week.
    @Query("""
           SELECT w.student.id FROM War w
           JOIN w.student s
           JOIN s.teams t
           WHERE t.id = :teamId
             AND w.weekStart = :weekStart
           """)
    List<Long> findStudentIdsWhoSubmittedWar(@Param("teamId") Long teamId,
                                              @Param("weekStart") LocalDate weekStart);
}