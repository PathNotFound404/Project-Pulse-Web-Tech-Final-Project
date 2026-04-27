package com.tcu.projectpulse.instructor;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
import com.tcu.projectpulse.instructor.dto.*;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import com.tcu.projectpulse.instructor.service.InstructorService;
import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import com.tcu.projectpulse.peerevaluation.repository.PeerEvaluationRepository;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.repository.WarRepository;
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

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    @Mock
    private PeerEvaluationRepository peerEvaluationRepository;

    @Mock
    private WarRepository warRepository;

    @InjectMocks
    private InstructorService instructorService;

    private Instructor activeInstructor;
    private Instructor deactivatedInstructor;
    private InvitationToken validToken;
    private InvitationToken usedToken;
    private Student student;

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

        validToken = new InvitationToken("valid-token-123", "j.smith@tcu.edu");

        usedToken = new InvitationToken("used-token-456", "used@tcu.edu");
        usedToken.setUsed(true);

        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
    }

    // -------------------------------------------------------
    // CODY'S TESTS (UC-18, 21, 22, 23, 24)
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // EDUARDA'S TESTS (UC-30, 31, 32, 33, 34)
    // -------------------------------------------------------

    @Test
    void registerInstructor_validToken_createsInstructor() {
        given(invitationTokenRepository.findByToken("valid-token-123"))
                .willReturn(Optional.of(validToken));
        given(instructorRepository.save(any(Instructor.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(invitationTokenRepository.save(any(InvitationToken.class)))
                .willAnswer(inv -> inv.getArgument(0));

        InstructorRegistrationRequest request = new InstructorRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPassword("password123");
        request.setReenterPassword("password123");

        InstructorResponse response = instructorService.registerInstructor("valid-token-123", request);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getEmail()).isEqualTo("j.smith@tcu.edu");
        assertThat(validToken.isUsed()).isTrue();
    }

    @Test
    void registerInstructor_usedToken_throwsIllegalStateException() {
        given(invitationTokenRepository.findByToken("used-token-456"))
                .willReturn(Optional.of(usedToken));

        InstructorRegistrationRequest request = new InstructorRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPassword("password123");
        request.setReenterPassword("password123");

        assertThatThrownBy(() -> instructorService.registerInstructor("used-token-456", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void registerInstructor_invalidToken_throwsObjectNotFoundException() {
        given(invitationTokenRepository.findByToken("bad-token"))
                .willReturn(Optional.empty());

        InstructorRegistrationRequest request = new InstructorRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPassword("password123");
        request.setReenterPassword("password123");

        assertThatThrownBy(() -> instructorService.registerInstructor("bad-token", request))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void registerInstructor_passwordMismatch_throwsIllegalArgumentException() {
        given(invitationTokenRepository.findByToken("valid-token-123"))
                .willReturn(Optional.of(validToken));

        InstructorRegistrationRequest request = new InstructorRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPassword("password123");
        request.setReenterPassword("differentPassword");

        assertThatThrownBy(() -> instructorService.registerInstructor("valid-token-123", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void generateSectionPeerEvalReport_hasData_returnsRows() {
        PeerEvaluation pe = new PeerEvaluation();
        pe.setStudent(student);
        pe.setActiveWeek("02-12-2024 to 02-18-2024");
        pe.setQualityOfWork(8);
        pe.setProductivity(9);
        pe.setInitiative(10);
        pe.setCourtesy(9);
        pe.setOpenMindedness(8);
        pe.setEngagementInMeetings(10);
        pe.setPublicComments("Great work!");

        given(peerEvaluationRepository.findBySectionIdAndActiveWeek(1L, "02-12-2024 to 02-18-2024"))
                .willReturn(List.of(pe));

        List<PeerEvalReportRow> rows = instructorService
                .generateSectionPeerEvalReport(1L, "02-12-2024 to 02-18-2024");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getStudentName()).isEqualTo("John Doe");
        assertThat(rows.get(0).getAverageGrade()).isEqualTo(54.0);
        assertThat(rows.get(0).getMaxGrade()).isEqualTo(60.0);
    }

    @Test
    void generateSectionPeerEvalReport_noData_returnsEmptyList() {
        given(peerEvaluationRepository.findBySectionIdAndActiveWeek(1L, "02-12-2024 to 02-18-2024"))
                .willReturn(List.of());

        List<PeerEvalReportRow> rows = instructorService
                .generateSectionPeerEvalReport(1L, "02-12-2024 to 02-18-2024");

        assertThat(rows).isEmpty();
    }

    @Test
    void generateTeamWarReport_hasData_returnsRows() {
        War war = new War();
        war.setStudent(student);
        war.setActiveWeek("02-12-2024 to 02-18-2024");
        war.setActivityCategory("DEVELOPMENT");
        war.setPlannedActivity("Fix login bug");
        war.setDescription("Fix the login bug");
        war.setPlannedHours(4.0);
        war.setActualHours(5.0);
        war.setStatus("Done");

        given(warRepository.findByTeamIdAndActiveWeek(1L, "02-12-2024 to 02-18-2024"))
                .willReturn(List.of(war));

        List<WarReportRow> rows = instructorService
                .generateTeamWarReport(1L, "02-12-2024 to 02-18-2024");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getStudentName()).isEqualTo("John Doe");
        assertThat(rows.get(0).getActivityCategory()).isEqualTo("DEVELOPMENT");
        assertThat(rows.get(0).getStatus()).isEqualTo("Done");
    }

    @Test
    void generateTeamWarReport_noData_returnsEmptyList() {
        given(warRepository.findByTeamIdAndActiveWeek(1L, "02-12-2024 to 02-18-2024"))
                .willReturn(List.of());

        List<WarReportRow> rows = instructorService
                .generateTeamWarReport(1L, "02-12-2024 to 02-18-2024");

        assertThat(rows).isEmpty();
    }

    @Test
    void generateStudentPeerEvalReport_hasData_returnsRowsGroupedByWeek() {
        PeerEvaluation pe = new PeerEvaluation();
        pe.setStudent(student);
        pe.setActiveWeek("02-12-2024 to 02-18-2024");
        pe.setQualityOfWork(10);
        pe.setProductivity(9);
        pe.setInitiative(10);
        pe.setCourtesy(9);
        pe.setOpenMindedness(8);
        pe.setEngagementInMeetings(9);
        pe.setPublicComments("Excellent!");

        given(peerEvaluationRepository.findByStudentIdAndWeekRange(1L, "02-12-2024", "04-28-2024"))
                .willReturn(List.of(pe));

        List<PeerEvalReportRow> rows = instructorService
                .generateStudentPeerEvalReport(1L, "02-12-2024", "04-28-2024");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getWeek()).isEqualTo("02-12-2024 to 02-18-2024");
        assertThat(rows.get(0).getAverageGrade()).isEqualTo(55.0);
    }

    @Test
    void generateStudentPeerEvalReport_noData_returnsEmptyList() {
        given(peerEvaluationRepository.findByStudentIdAndWeekRange(1L, "02-12-2024", "04-28-2024"))
                .willReturn(List.of());

        List<PeerEvalReportRow> rows = instructorService
                .generateStudentPeerEvalReport(1L, "02-12-2024", "04-28-2024");

        assertThat(rows).isEmpty();
    }

    @Test
    void generateStudentWarReport_hasData_returnsRowsSortedByWeek() {
        War war = new War();
        war.setStudent(student);
        war.setActiveWeek("02-12-2024 to 02-18-2024");
        war.setActivityCategory("DEVELOPMENT");
        war.setPlannedActivity("Activity 1");
        war.setPlannedHours(4.0);
        war.setActualHours(5.0);
        war.setStatus("Done");

        given(warRepository.findByStudentIdAndWeekRange(1L, "02-12-2024", "04-28-2024"))
                .willReturn(List.of(war));

        List<WarReportRow> rows = instructorService
                .generateStudentWarReport(1L, "02-12-2024", "04-28-2024");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getWeek()).isEqualTo("02-12-2024 to 02-18-2024");
        assertThat(rows.get(0).getActivityCategory()).isEqualTo("DEVELOPMENT");
    }

    @Test
    void generateStudentWarReport_noData_returnsEmptyList() {
        given(warRepository.findByStudentIdAndWeekRange(1L, "02-12-2024", "04-28-2024"))
                .willReturn(List.of());

        List<WarReportRow> rows = instructorService
                .generateStudentWarReport(1L, "02-12-2024", "04-28-2024");

        assertThat(rows).isEmpty();
    }
}