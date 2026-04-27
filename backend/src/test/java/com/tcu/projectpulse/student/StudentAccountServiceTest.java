package com.tcu.projectpulse.student;

import com.tcu.projectpulse.common.exception.AccountAlreadyExistsException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.dto.UpdateStudentRequest;
import com.tcu.projectpulse.student.dto.UserProfileDto;
import com.tcu.projectpulse.student.repository.StudentInvitationTokenRepository;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.student.service.StudentService;
import com.tcu.projectpulse.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentAccountServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private StudentInvitationTokenRepository tokenRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");
        student.setPasswordHash("$2a$10$hashedvalue");
    }

    @Test
    void getProfile_returnsCorrectStudentProfile() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        UserProfileDto result = studentService.getProfile(1L);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.email()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void update_validRequest_updatesAndReturnsProfile() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));
        given(studentRepository.existsByEmail("janet.jones@example.com")).willReturn(false);

        UserProfileDto result = studentService.update(1L,
                new UpdateStudentRequest("Janet", "Jones", "janet.jones@example.com"));

        assertThat(result.firstName()).isEqualTo("Janet");
        assertThat(result.lastName()).isEqualTo("Jones");
        assertThat(result.email()).isEqualTo("janet.jones@example.com");
    }

    @Test
    void update_emailTakenByAnotherStudent_throwsAccountAlreadyExistsException() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));
        given(studentRepository.existsByEmail("taken@example.com")).willReturn(true);

        assertThatThrownBy(() -> studentService.update(1L,
                new UpdateStudentRequest("Jane", "Smith", "taken@example.com")))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessageContaining("already in use");
    }
}
