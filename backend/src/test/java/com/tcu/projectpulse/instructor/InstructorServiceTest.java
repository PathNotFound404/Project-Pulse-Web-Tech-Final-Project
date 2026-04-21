package com.tcu.projectpulse.instructor;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
import com.tcu.projectpulse.instructor.dto.InstructorDetailDto;
import com.tcu.projectpulse.instructor.dto.InstructorSearchCriteria;
import com.tcu.projectpulse.instructor.dto.InstructorSummaryDto;
import com.tcu.projectpulse.instructor.dto.InviteLinkDto;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import com.tcu.projectpulse.instructor.service.InstructorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    @InjectMocks
    private InstructorService instructorService;

    private Instructor activeInstructor;
    private Instructor deactivatedInstructor;

    @BeforeEach
    void setUp() {
        activeInstructor = new Instructor();
        activeInstructor.setId(1L);
        activeInstructor.setFirstName("Jane");
        activeInstructor.setLastName("Smith");
        activeInstructor.setEmail("j.smith@tcu.edu");
        activeInstructor.setStatus(InstructorStatus.ACTIVE);
        activeInstructor.setTeams(new ArrayList<>());

        deactivatedInstructor = new Instructor();
        deactivatedInstructor.setId(2L);
        deactivatedInstructor.setFirstName("Bob");
        deactivatedInstructor.setLastName("Jones");
        deactivatedInstructor.setEmail("b.jones@tcu.edu");
        deactivatedInstructor.setStatus(InstructorStatus.DEACTIVATED);
        deactivatedInstructor.setTeams(new ArrayList<>());
    }

    @Test
    void findInstructors_byStatusActive_returnsOnlyActive() {
        given(instructorRepository.findAll(any(Specification.class))).willReturn(List.of(activeInstructor));

        InstructorSearchCriteria criteria = new InstructorSearchCriteria();
        criteria.setStatus(InstructorStatus.ACTIVE);

        List<InstructorSummaryDto> result = instructorService.findInstructors(criteria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(InstructorStatus.ACTIVE);
    }

    @Test
    void findInstructors_noMatch_returnsEmptyList() {
        given(instructorRepository.findAll(any(Specification.class))).willReturn(List.of());

        List<InstructorSummaryDto> result = instructorService.findInstructors(new InstructorSearchCriteria());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_exists_returnsInstructorDetailDto() {
        given(instructorRepository.findById(1L)).willReturn(Optional.of(activeInstructor));

        InstructorDetailDto dto = instructorService.findById(1L);

        assertThat(dto.firstName()).isEqualTo("Jane");
        assertThat(dto.status()).isEqualTo(InstructorStatus.ACTIVE);
    }

    @Test
    void findById_notFound_throwsObjectNotFoundException() {
        given(instructorRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> instructorService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void generateInviteLinks_validEmails_returnsLinksWithTokens() {
        given(invitationTokenRepository.save(any(InvitationToken.class))).willAnswer(inv -> inv.getArgument(0));

        List<InviteLinkDto> links = instructorService.generateInviteLinks(List.of("a@tcu.edu", "b@tcu.edu"));

        assertThat(links).hasSize(2);
        assertThat(links.get(0).email()).isEqualTo("a@tcu.edu");
        assertThat(links.get(0).inviteLink()).contains("token=");
        assertThat(links.get(1).email()).isEqualTo("b@tcu.edu");
    }

    @Test
    void deactivate_activeInstructor_setsDeactivated() {
        given(instructorRepository.findById(1L)).willReturn(Optional.of(activeInstructor));

        InstructorDetailDto dto = instructorService.deactivate(1L);

        assertThat(dto.status()).isEqualTo(InstructorStatus.DEACTIVATED);
        assertThat(activeInstructor.getStatus()).isEqualTo(InstructorStatus.DEACTIVATED);
    }

    @Test
    void deactivate_alreadyDeactivated_throwsIllegalStateException() {
        given(instructorRepository.findById(2L)).willReturn(Optional.of(deactivatedInstructor));

        assertThatThrownBy(() -> instructorService.deactivate(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already deactivated");
    }

    @Test
    void reactivate_deactivatedInstructor_setsActive() {
        given(instructorRepository.findById(2L)).willReturn(Optional.of(deactivatedInstructor));

        InstructorDetailDto dto = instructorService.reactivate(2L);

        assertThat(dto.status()).isEqualTo(InstructorStatus.ACTIVE);
        assertThat(deactivatedInstructor.getStatus()).isEqualTo(InstructorStatus.ACTIVE);
    }

    @Test
    void reactivate_alreadyActive_throwsIllegalStateException() {
        given(instructorRepository.findById(1L)).willReturn(Optional.of(activeInstructor));

        assertThatThrownBy(() -> instructorService.reactivate(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }
}
