package com.tcu.projectpulse.auth;

import com.tcu.projectpulse.common.exception.AccountAlreadyExistsException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.domain.StudentInvitationToken;
import com.tcu.projectpulse.student.dto.RegisterStudentRequest;
import com.tcu.projectpulse.student.repository.StudentInvitationTokenRepository;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.student.service.StudentService;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentRegistrationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private StudentInvitationTokenRepository tokenRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void register_validInputs_savesStudent() {
        String token = "valid-token-abc";
        StudentInvitationToken invitation = new StudentInvitationToken(token, "invited@example.com");

        given(tokenRepository.findByToken(token)).willReturn(Optional.of(invitation));
        given(studentRepository.existsByEmail("invited@example.com")).willReturn(false);

        Student saved = new Student();
        saved.setId(1L);
        saved.setFirstName("Jane");
        saved.setLastName("Smith");
        saved.setEmail("invited@example.com");
        saved.setPasswordHash("$2a$10$hashedvalue");
        given(studentRepository.save(any(Student.class))).willReturn(saved);

        studentService.register(token, new RegisterStudentRequest("Jane", "Smith", "jane@example.com", "securePass1"));

        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void register_tokenAlreadyUsed_throwsAccountAlreadyExistsException() {
        String token = "used-token-xyz";
        StudentInvitationToken usedInvitation = new StudentInvitationToken(token, "invited@example.com");
        usedInvitation.setUsed(true);

        given(tokenRepository.findByToken(token)).willReturn(Optional.of(usedInvitation));

        assertThatThrownBy(() -> studentService.register(token,
                new RegisterStudentRequest("Jane", "Smith", "jane@example.com", "securePass1")))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessageContaining("Account already created");
    }

    @Test
    void register_validInputs_passwordHashUsesBcrypt() {
        String rawPassword = "myPassword9";
        String token = "valid-token-bcrypt";
        StudentInvitationToken invitation = new StudentInvitationToken(token, "bcrypt@example.com");

        given(tokenRepository.findByToken(token)).willReturn(Optional.of(invitation));
        given(studentRepository.existsByEmail("bcrypt@example.com")).willReturn(false);

        Student saved = new Student();
        saved.setId(2L);
        saved.setFirstName("Test");
        saved.setLastName("User");
        saved.setEmail("bcrypt@example.com");
        saved.setPasswordHash("$2a$10$somehash");
        given(studentRepository.save(any(Student.class))).willReturn(saved);

        studentService.register(token, new RegisterStudentRequest("Test", "User", "test@example.com", rawPassword));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());

        String storedHash = captor.getValue().getPasswordHash();
        assertThat(storedHash).isNotEqualTo(rawPassword);
        assertThat(storedHash).startsWith("$2a$");
    }
}
