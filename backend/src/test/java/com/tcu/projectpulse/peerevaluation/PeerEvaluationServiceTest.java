package com.tcu.projectpulse.peerevaluation;

import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import com.tcu.projectpulse.peerevaluation.dto.EvaluationEntryRequest;
import com.tcu.projectpulse.peerevaluation.dto.EvaluationEntryResponse;
import com.tcu.projectpulse.peerevaluation.dto.PeerEvaluationSheetResponse;
import com.tcu.projectpulse.peerevaluation.repository.PeerEvaluationRepository;
import com.tcu.projectpulse.peerevaluation.service.PeerEvaluationService;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PeerEvaluationServiceTest {

    @Mock private PeerEvaluationRepository peerEvaluationRepository;
    @Mock private StudentRepository studentRepository;

    @InjectMocks
    private PeerEvaluationService peerEvaluationService;

    private Section section;
    private Team team;
    private Student alice;
    private Student bob;
    private Student carol;
    private LocalDate weekStart;

    @BeforeEach
    void setUp() {
        section = new Section("2024-2025");
        section.setId(1L);

        alice = new Student();
        alice.setId(1L);
        alice.setFirstName("Alice");
        alice.setLastName("Adams");
        alice.setEmail("alice@tcu.edu");
        alice.setSection(section);

        bob = new Student();
        bob.setId(2L);
        bob.setFirstName("Bob");
        bob.setLastName("Baker");
        bob.setEmail("bob@tcu.edu");
        bob.setSection(section);

        carol = new Student();
        carol.setId(3L);
        carol.setFirstName("Carol");
        carol.setLastName("Clark");
        carol.setEmail("carol@tcu.edu");
        carol.setSection(section);

        team = new Team();
        team.setId(1L);
        team.setName("Team PE");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(alice, bob, carol)));
        team.setInstructors(new ArrayList<>());

        alice.setTeams(new ArrayList<>(List.of(team)));
        bob.setTeams(new ArrayList<>(List.of(team)));
        carol.setTeams(new ArrayList<>(List.of(team)));

        weekStart = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
    }

    @Test
    void getSheet_returnsAllTeammatesIncludingSelf() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));
        given(peerEvaluationRepository.findByStudentIdAndWeekStart(1L, weekStart))
                .willReturn(List.of());

        PeerEvaluationSheetResponse result = peerEvaluationService.getSheet(1L, weekStart);

        assertThat(result.entries()).hasSize(3);
        assertThat(result.entries().stream().map(EvaluationEntryResponse::firstName))
                .containsExactlyInAnyOrder("Alice", "Bob", "Carol");
    }

    @Test
    void getSheet_studentNotOnTeam_throwsIllegalStateException() {
        alice.setTeams(new ArrayList<>());
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));

        assertThatThrownBy(() -> peerEvaluationService.getSheet(1L, weekStart))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not assigned to a team");
    }

    @Test
    void submit_savesEvaluationsForAllTeammates() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));
        given(studentRepository.findById(2L)).willReturn(Optional.of(bob));
        given(studentRepository.findById(3L)).willReturn(Optional.of(carol));

        peerEvaluationService.submit(1L, weekStart, List.of(
                entry(1L, 7), entry(2L, 7), entry(3L, 7)
        ));

        verify(peerEvaluationRepository, times(3)).save(any(PeerEvaluation.class));
    }

    @Test
    void submit_missingTeammate_throwsIllegalArgumentException() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));

        assertThatThrownBy(() -> peerEvaluationService.submit(1L, weekStart, List.of(
                entry(1L, 7), entry(2L, 7)
                // carol missing
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing evaluations");
    }

    @Test
    void submit_scoreBelowOne_throwsIllegalArgumentException() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));

        assertThatThrownBy(() -> peerEvaluationService.submit(1L, weekStart, List.of(
                entry(1L, 0), entry(2L, 7), entry(3L, 7)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 10");
    }

    @Test
    void submit_scoreAboveTen_throwsIllegalArgumentException() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));

        assertThatThrownBy(() -> peerEvaluationService.submit(1L, weekStart, List.of(
                entry(1L, 11), entry(2L, 7), entry(3L, 7)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 10");
    }

    @Test
    void submit_resubmission_overwritesPreviousEvaluation() {
        PeerEvaluation existing = new PeerEvaluation();
        existing.setId(99L);
        existing.setStudent(alice);
        existing.setEvaluatee(alice);
        existing.setWeekStart(weekStart);
        existing.setWeekEnd(weekStart.plusDays(6));

        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));
        given(studentRepository.findById(2L)).willReturn(Optional.of(bob));
        given(studentRepository.findById(3L)).willReturn(Optional.of(carol));
        given(peerEvaluationRepository.findByStudentIdAndEvaluateeIdAndWeekStart(1L, 1L, weekStart))
                .willReturn(Optional.of(existing));

        peerEvaluationService.submit(1L, weekStart, List.of(
                entry(1L, 8), entry(2L, 8), entry(3L, 8)
        ));

        ArgumentCaptor<PeerEvaluation> captor = ArgumentCaptor.forClass(PeerEvaluation.class);
        verify(peerEvaluationRepository, times(3)).save(captor.capture());

        PeerEvaluation resubmitted = captor.getAllValues().stream()
                .filter(pe -> Long.valueOf(99L).equals(pe.getId()))
                .findFirst().orElseThrow();
        assertThat(resubmitted.getQualityOfWork()).isEqualTo(8);
    }

    @Test
    void submit_privateCommentSavedToDatabaseButNotInResponse() {
        given(studentRepository.findById(1L)).willReturn(Optional.of(alice));
        given(studentRepository.findById(2L)).willReturn(Optional.of(bob));
        given(studentRepository.findById(3L)).willReturn(Optional.of(carol));

        List<EvaluationEntryRequest> entries = List.of(
                new EvaluationEntryRequest(1L, 7, 7, 7, 7, 7, 7, "public", "secret"),
                new EvaluationEntryRequest(2L, 7, 7, 7, 7, 7, 7, "public", "secret"),
                new EvaluationEntryRequest(3L, 7, 7, 7, 7, 7, 7, "public", "secret")
        );

        PeerEvaluationSheetResponse result = peerEvaluationService.submit(1L, weekStart, entries);

        // private comment IS saved to the database
        ArgumentCaptor<PeerEvaluation> captor = ArgumentCaptor.forClass(PeerEvaluation.class);
        verify(peerEvaluationRepository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allMatch(pe -> "secret".equals(pe.getPrivateComment()));

        // private comment is NOT present on the response DTO
        assertThat(result.entries()).hasSize(3);
        assertThat(EvaluationEntryResponse.class.getDeclaredFields())
                .noneMatch(f -> f.getName().equals("privateComment"));
    }

    private EvaluationEntryRequest entry(Long evaluateeId, int score) {
        return new EvaluationEntryRequest(
                evaluateeId, score, score, score, score, score, score, "Good work", null
        );
    }
}
