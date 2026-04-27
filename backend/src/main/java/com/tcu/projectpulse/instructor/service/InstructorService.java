package com.tcu.projectpulse.instructor.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
import com.tcu.projectpulse.instructor.dto.*;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import com.tcu.projectpulse.peerevaluation.repository.PeerEvaluationRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.repository.WarRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PeerEvaluationRepository peerEvaluationRepository;
    private final WarRepository warRepository;

    @Value("${server.port:8080}")
    private int serverPort;

    public InstructorService(InstructorRepository instructorRepository,
                             InvitationTokenRepository invitationTokenRepository,
                             PeerEvaluationRepository peerEvaluationRepository,
                             WarRepository warRepository) {
        this.instructorRepository = instructorRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.peerEvaluationRepository = peerEvaluationRepository;
        this.warRepository = warRepository;
    }

    // -------------------------------------------------------
    // CODY'S USE CASES (UC-18, 21, 22, 23, 24)
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public List<InstructorSummaryDto> findInstructors(InstructorSearchCriteria criteria) {
        InstructorSpecification spec = new InstructorSpecification(criteria);
        List<Instructor> instructors = instructorRepository.findAll(spec);

        return instructors.stream()
                .sorted(Comparator.comparing(Instructor::getLastName))
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstructorDetailDto findById(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Instructor", id));
        return toDetailDto(instructor);
    }

    public List<InviteLinkDto> generateInviteLinks(List<String> emails) {
        List<InviteLinkDto> links = new ArrayList<>();
        for (String email : emails) {
            String trimmed = email.trim();
            if (trimmed.isBlank()) continue;

            String token = UUID.randomUUID().toString();
            InvitationToken invitationToken = new InvitationToken(token, trimmed);
            invitationTokenRepository.save(invitationToken);

            String link = "http://localhost:" + serverPort + "/api/instructors/register?token=" + token;
            links.add(new InviteLinkDto(trimmed, link));
        }
        return links;
    }

    public InstructorDetailDto deactivate(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Instructor", id));

        if (instructor.getStatus() == InstructorStatus.DEACTIVATED) {
            throw new IllegalStateException("Instructor " + id + " is already deactivated");
        }

        instructor.setStatus(InstructorStatus.DEACTIVATED);
        return toDetailDto(instructor);
    }

    public InstructorDetailDto reactivate(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Instructor", id));

        if (instructor.getStatus() == InstructorStatus.ACTIVE) {
            throw new IllegalStateException("Instructor " + id + " is already active");
        }

        instructor.setStatus(InstructorStatus.ACTIVE);
        return toDetailDto(instructor);
    }

    // -------------------------------------------------------
    // EDUARDA'S USE CASES (UC-30, 31, 32, 33, 34)
    // -------------------------------------------------------

    // UC-30: Instructor sets up account using invite link token
    public InstructorResponse registerInstructor(String token, InstructorRegistrationRequest request) {
        InvitationToken invitationToken = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ObjectNotFoundException("InvitationToken", token));

        // UC-30 Extension 2a: Already registered
        if (invitationToken.isUsed()) {
            throw new IllegalStateException("This invitation link has already been used. Please log in.");
        }

        if (!request.getPassword().equals(request.getReenterPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Instructor instructor = new Instructor();
        instructor.setFirstName(request.getFirstName());
        instructor.setLastName(request.getLastName());
        instructor.setEmail(invitationToken.getEmail());
        instructor.setStatus(InstructorStatus.ACTIVE);

        Instructor saved = instructorRepository.save(instructor);
        invitationToken.setUsed(true);
        invitationTokenRepository.save(invitationToken);

        return toResponse(saved);
    }

    // UC-31: Generate peer eval report for entire section for a given week
    // Algorithm: for each student, average the total scores from all evaluators
    @Transactional(readOnly = true)
    public List<PeerEvalReportRow> generateSectionPeerEvalReport(Long sectionId, String activeWeek) {
        List<PeerEvaluation> evals = peerEvaluationRepository.findBySectionIdAndActiveWeek(sectionId, activeWeek);

        Map<Long, List<PeerEvaluation>> byStudent = evals.stream()
                .collect(Collectors.groupingBy(pe -> pe.getStudent().getId()));

        List<PeerEvalReportRow> rows = new ArrayList<>();
        for (Map.Entry<Long, List<PeerEvaluation>> entry : byStudent.entrySet()) {
            List<PeerEvaluation> studentEvals = entry.getValue();
            PeerEvaluation first = studentEvals.get(0);

            PeerEvalReportRow row = new PeerEvalReportRow();
            row.setStudentName(first.getStudent().getFirstName() + " " + first.getStudent().getLastName());

            double avgGrade = studentEvals.stream()
                    .mapToInt(PeerEvaluation::getTotalScore)
                    .average().orElse(0.0);
            row.setAverageGrade(Math.round(avgGrade * 10.0) / 10.0);
            row.setMaxGrade((double) first.getMaxScore());
            row.setSubmittedEval(true);

            List<String> publicComments = studentEvals.stream()
                    .filter(pe -> pe.getPublicComments() != null && !pe.getPublicComments().isBlank())
                    .map(PeerEvaluation::getPublicComments)
                    .collect(Collectors.toList());
            row.setPublicComments(publicComments);
            rows.add(row);
        }

        rows.sort(Comparator.comparing(PeerEvalReportRow::getStudentName));
        return rows;
    }

    // UC-32: Generate WAR report for a team for a given week
    @Transactional(readOnly = true)
    public List<WarReportRow> generateTeamWarReport(Long teamId, String activeWeek) {
        List<War> wars = warRepository.findByTeamIdAndActiveWeek(teamId, activeWeek);

        List<WarReportRow> rows = wars.stream().map(war -> {
            WarReportRow row = new WarReportRow();
            row.setStudentName(war.getStudent().getFirstName() + " " + war.getStudent().getLastName());
            row.setActivityCategory(war.getActivityCategory());
            row.setPlannedActivity(war.getPlannedActivity());
            row.setDescription(war.getDescription());
            row.setPlannedHours(war.getPlannedHours());
            row.setActualHours(war.getActualHours());
            row.setStatus(war.getStatus());
            row.setSubmittedWar(true);
            return row;
        }).collect(Collectors.toList());

        rows.sort(Comparator.comparing(WarReportRow::getStudentName));
        return rows;
    }

    // UC-33: Generate peer eval report for a student over a date range
    @Transactional(readOnly = true)
    public List<PeerEvalReportRow> generateStudentPeerEvalReport(Long studentId,
                                                                  String startWeek,
                                                                  String endWeek) {
        List<PeerEvaluation> evals = peerEvaluationRepository
                .findByStudentIdAndWeekRange(studentId, startWeek, endWeek);

        Map<String, List<PeerEvaluation>> byWeek = evals.stream()
                .collect(Collectors.groupingBy(PeerEvaluation::getActiveWeek));

        List<PeerEvalReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<PeerEvaluation>> entry : byWeek.entrySet()) {
            List<PeerEvaluation> weekEvals = entry.getValue();

            PeerEvalReportRow row = new PeerEvalReportRow();
            row.setWeek(entry.getKey());

            double avgGrade = weekEvals.stream()
                    .mapToInt(PeerEvaluation::getTotalScore)
                    .average().orElse(0.0);
            row.setAverageGrade(Math.round(avgGrade * 10.0) / 10.0);
            row.setMaxGrade((double) weekEvals.get(0).getMaxScore());
            row.setSubmittedEval(true);

            List<String> publicComments = weekEvals.stream()
                    .filter(pe -> pe.getPublicComments() != null && !pe.getPublicComments().isBlank())
                    .map(PeerEvaluation::getPublicComments)
                    .collect(Collectors.toList());
            row.setPublicComments(publicComments);
            rows.add(row);
        }

        rows.sort(Comparator.comparing(PeerEvalReportRow::getWeek));
        return rows;
    }

    // UC-34: Generate WAR report for a student over a date range
    @Transactional(readOnly = true)
    public List<WarReportRow> generateStudentWarReport(Long studentId,
                                                        String startWeek,
                                                        String endWeek) {
        List<War> wars = warRepository.findByStudentIdAndWeekRange(studentId, startWeek, endWeek);

        return wars.stream().map(war -> {
            WarReportRow row = new WarReportRow();
            row.setWeek(war.getActiveWeek());
            row.setActivityCategory(war.getActivityCategory());
            row.setPlannedActivity(war.getPlannedActivity());
            row.setDescription(war.getDescription());
            row.setPlannedHours(war.getPlannedHours());
            row.setActualHours(war.getActualHours());
            row.setStatus(war.getStatus());
            row.setSubmittedWar(true);
            return row;
        }).sorted(Comparator.comparing(WarReportRow::getWeek))
          .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------

    private InstructorSummaryDto toSummaryDto(Instructor instructor) {
        List<String> teamNames = instructor.getTeams().stream().map(Team::getName).toList();
        return new InstructorSummaryDto(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                teamNames,
                instructor.getStatus()
        );
    }

    private InstructorDetailDto toDetailDto(Instructor instructor) {
        Map<String, List<String>> bySection = instructor.getTeams().stream()
                .collect(Collectors.groupingBy(
                        team -> team.getSection() != null ? team.getSection().getName() : "No Section",
                        Collectors.mapping(Team::getName, Collectors.toList())
                ));
        return new InstructorDetailDto(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                instructor.getStatus(),
                bySection
        );
    }

    private InstructorResponse toResponse(Instructor instructor) {
        InstructorResponse response = new InstructorResponse();
        response.setId(instructor.getId());
        response.setFirstName(instructor.getFirstName());
        response.setLastName(instructor.getLastName());
        response.setEmail(instructor.getEmail());
        response.setStatus(instructor.getStatus());
        if (instructor.getTeams() != null) {
            response.setTeamNames(
                instructor.getTeams().stream()
                    .map(Team::getName)
                    .toList()
            );
        }
        return response;
    }
}