package com.tcu.projectpulse.war.repository;

import com.tcu.projectpulse.war.domain.War;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarRepository extends JpaRepository<War, Long> {

    // UC-32: Get all WAR entries for a given week
    // Note: teamId filtering done in service layer since Student->team relationship unknown
    @Query("SELECT w FROM War w WHERE w.activeWeek = :activeWeek")
    List<War> findByTeamIdAndActiveWeek(@Param("teamId") Long teamId,
                                        @Param("activeWeek") String activeWeek);

    // UC-34: Get all WAR entries for a student in a date range
    @Query("SELECT w FROM War w WHERE w.student.id = :studentId AND w.activeWeek BETWEEN :startWeek AND :endWeek")
    List<War> findByStudentIdAndWeekRange(@Param("studentId") Long studentId,
                                          @Param("startWeek") String startWeek,
                                          @Param("endWeek") String endWeek);

    // UC-32: Check which students submitted WAR for a week
    @Query("SELECT w.student.id FROM War w WHERE w.activeWeek = :activeWeek")
    List<Long> findStudentIdsWhoSubmittedWar(@Param("teamId") Long teamId,
                                              @Param("activeWeek") String activeWeek);
}