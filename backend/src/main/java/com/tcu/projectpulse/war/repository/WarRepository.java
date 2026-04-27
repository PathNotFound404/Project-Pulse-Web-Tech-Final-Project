package com.tcu.projectpulse.war.repository;

import com.tcu.projectpulse.war.domain.War;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WarRepository extends JpaRepository<War, Long> {

    Optional<War> findByStudentIdAndWeekStart(Long studentId, LocalDate weekStart);
}
