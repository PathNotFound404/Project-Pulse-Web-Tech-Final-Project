package com.tcu.projectpulse.student.repository;

import com.tcu.projectpulse.student.domain.StudentInvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentInvitationTokenRepository extends JpaRepository<StudentInvitationToken, Long> {

    Optional<StudentInvitationToken> findByToken(String token);

    boolean existsByEmail(String email);
}
