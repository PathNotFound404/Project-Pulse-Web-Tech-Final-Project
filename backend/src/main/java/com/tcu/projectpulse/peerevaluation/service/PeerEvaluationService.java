package com.tcu.projectpulse.peerevaluation.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import com.tcu.projectpulse.peerevaluation.dto.EvaluationEntryRequest;
import com.tcu.projectpulse.peerevaluation.dto.EvaluationEntryResponse;
import com.tcu.projectpulse.peerevaluation.dto.PeerEvaluationReportResponse;
import com.tcu.projectpulse.peerevaluation.dto.PeerEvaluationSheetResponse;
import com.tcu.projectpulse.peerevaluation.repository.PeerEvaluationRepository;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PeerEvaluationService {

    private final PeerEvaluationRepository peerEvaluationRepository;
    private final StudentRepository studentRepository;

    public PeerEvaluationService(PeerEvaluationRepository peerEvaluationRepository,
                                 StudentRepository studentRepository) {
        this.peerEvaluationRepository = peerEvaluationRepository;
        this.studentRepository = studentRepository;
    }

    // UC-28: Get teammates and existing evaluation scores for a given week
    @Transactional(readOnly = true)
    public PeerEvaluationSheetResponse getSheet(Long studentId, LocalDate anyDateInWeek) {
        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));

        List<Student> teammates = getTeammates(student);

        List<PeerEvaluation> existing =
                peerEvaluationRepository.findByStudentIdAndWeekStart(studentId, weekStart);

        List<EvaluationEntryResponse> entries = teammates.stream()
                .map(tm -> {
                    PeerEvaluation pe = existing.stream()
                            .filter(e -> e.getEvaluatee().getId().equals(tm.getId()))
                            .findFirst()
                            .orElse(null);
                    return toEntryResponse(tm, pe);
                })
                .toList();

        return new PeerEvaluationSheetResponse(weekStart, weekEnd, entries);
    }

    // UC-28: Submit or update evaluations for the week
    public PeerEvaluationSheetResponse submit(Long studentId, LocalDate weekStart,
                                              List<EvaluationEntryRequest> entries) {
        weekStart = weekStart.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        Student evaluator = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));

        List<Student> teammates = getTeammates(evaluator);
        validateAllTeammatesCovered(teammates, entries);

        for (EvaluationEntryRequest entry : entries) {
            validateScores(entry);

            Student evaluatee = studentRepository.findById(entry.evaluateeId())
                    .orElseThrow(() -> new ObjectNotFoundException("Student", entry.evaluateeId()));

            PeerEvaluation pe = peerEvaluationRepository
                    .findByStudentIdAndEvaluateeIdAndWeekStart(studentId, entry.evaluateeId(), weekStart)
                    .orElseGet(PeerEvaluation::new);

            pe.setStudent(evaluator);
            pe.setEvaluatee(evaluatee);
            pe.setWeekStart(weekStart);
            pe.setWeekEnd(weekEnd);
            pe.setQualityOfWork(entry.qualityOfWork());
            pe.setProductivity(entry.productivity());
            pe.setProactiveness(entry.proactiveness());
            pe.setTreatsOthersWithRespect(entry.treatsOthersWithRespect());
            pe.setHandlesCriticism(entry.handlesCriticism());
            pe.setPerformanceInMeetings(entry.performanceInMeetings());
            pe.setPublicComment(entry.publicComment());
            pe.setPrivateComment(entry.privateComment());
            pe.setSubmittedAt(LocalDateTime.now());

            peerEvaluationRepository.save(pe);
        }

        return getSheet(studentId, weekStart);
    }

    // UC-29: View own peer evaluation report for a given week
    @Transactional(readOnly = true)
    public PeerEvaluationReportResponse getMyReport(Long studentId, LocalDate anyDateInWeek) {
        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Student", studentId));

        List<PeerEvaluation> received =
                peerEvaluationRepository.findByEvaluateeIdAndWeekStart(studentId, weekStart);

        if (received.isEmpty()) {
            return new PeerEvaluationReportResponse(
                    weekStart, weekEnd,
                    student.getFirstName(), student.getLastName(),
                    null, null, null, null, null, null,
                    List.of(), null
            );
        }

        double avgQoW  = received.stream().mapToInt(PeerEvaluation::getQualityOfWork).average().orElse(0);
        double avgProd = received.stream().mapToInt(PeerEvaluation::getProductivity).average().orElse(0);
        double avgProa = received.stream().mapToInt(PeerEvaluation::getProactiveness).average().orElse(0);
        double avgResp = received.stream().mapToInt(PeerEvaluation::getTreatsOthersWithRespect).average().orElse(0);
        double avgCrit = received.stream().mapToInt(PeerEvaluation::getHandlesCriticism).average().orElse(0);
        double avgMeet = received.stream().mapToInt(PeerEvaluation::getPerformanceInMeetings).average().orElse(0);

        List<String> publicComments = received.stream()
                .map(PeerEvaluation::getPublicComment)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        double grade = received.stream()
                .mapToInt(pe -> pe.getQualityOfWork() + pe.getProductivity() + pe.getProactiveness()
                        + pe.getTreatsOthersWithRespect() + pe.getHandlesCriticism() + pe.getPerformanceInMeetings())
                .average()
                .orElse(0);

        return new PeerEvaluationReportResponse(
                weekStart, weekEnd,
                student.getFirstName(), student.getLastName(),
                avgQoW, avgProd, avgProa, avgResp, avgCrit, avgMeet,
                publicComments, grade
        );
    }

    private List<Student> getTeammates(Student student) {
        List<Team> teams = student.getTeams();
        if (teams.isEmpty()) {
            throw new IllegalStateException("You are not assigned to a team.");
        }
        return teams.get(0).getStudents();
    }

    private void validateAllTeammatesCovered(List<Student> teammates, List<EvaluationEntryRequest> entries) {
        Set<Long> required = teammates.stream().map(Student::getId).collect(Collectors.toSet());
        Set<Long> provided = entries.stream().map(EvaluationEntryRequest::evaluateeId).collect(Collectors.toSet());

        Set<Long> missing = required.stream().filter(id -> !provided.contains(id)).collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing evaluations for teammate IDs: " + missing);
        }

        Set<Long> unknown = provided.stream().filter(id -> !required.contains(id)).collect(Collectors.toSet());
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown evaluatee IDs: " + unknown);
        }
    }

    private void validateScores(EvaluationEntryRequest entry) {
        validateScore("Quality of work", entry.qualityOfWork());
        validateScore("Productivity", entry.productivity());
        validateScore("Proactiveness", entry.proactiveness());
        validateScore("Treats others with respect", entry.treatsOthersWithRespect());
        validateScore("Handles criticism", entry.handlesCriticism());
        validateScore("Performance in meetings", entry.performanceInMeetings());
    }

    private void validateScore(String fieldName, Integer score) {
        if (score == null) {
            throw new IllegalArgumentException(fieldName + " score is required");
        }
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException(fieldName + " score must be between 1 and 10");
        }
    }

    private EvaluationEntryResponse toEntryResponse(Student teammate, PeerEvaluation pe) {
        return new EvaluationEntryResponse(
                teammate.getId(),
                teammate.getFirstName(),
                teammate.getLastName(),
                pe != null ? pe.getQualityOfWork() : null,
                pe != null ? pe.getProductivity() : null,
                pe != null ? pe.getProactiveness() : null,
                pe != null ? pe.getTreatsOthersWithRespect() : null,
                pe != null ? pe.getHandlesCriticism() : null,
                pe != null ? pe.getPerformanceInMeetings() : null,
                pe != null ? pe.getPublicComment() : null
        );
    }
}
