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
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
import com.tcu.projectpulse.war.domain.Activity;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.repository.WarRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InstructorService {

    private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final InstructorRepository instructorRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PeerEvaluationRepository peerEvaluationRepository;
    private final WarRepository warRepository;
    private final TeamRepository teamRepository;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public InstructorService(InstructorRepository instructorRepository,
                             InvitationTokenRepository invitationTokenRepository,
                             PeerEvaluationRepository peerEvaluationRepository,
                             WarRepository warRepository,
                             TeamRepository teamRepository) {
        this.instructorRepository = instructorRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.peerEvaluationRepository = peerEvaluationRepository;
        this.warRepository = warRepository;
        this.teamRepository = teamRepository;
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
            String link = frontendBaseUrl + "/instructor/register?token=" + token;
            links.add(new InviteLinkDto(trimmed, link));
        }
        return links;
    }

    public InstructorDetailDto deactivate(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Instructor", id));
        if (instructor.getStatus() == InstructorStatus.DEACTIVATED)
            throw new IllegalStateException("Instructor " + id + " is already deactivated");
        instructor.setStatus(InstructorStatus.DEACTIVATED);
        return toDetailDto(instructor);
    }

    public InstructorDetailDto reactivate(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Instructor", id));
        if (instructor.getStatus() == InstructorStatus.ACTIVE)
            throw new IllegalStateException("Instructor " + id + " is already active");
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

        if (invitationToken.isUsed())
            throw new IllegalStateException("This invitation link has already been used. Please log in.");

        if (!request.getPassword().equals(request.getReenterPassword()))
            throw new IllegalArgumentException("Passwords do not match");

        Instructor instructor = new Instructor();
        instructor.setFirstName(request.getFirstName());
        instructor.setMiddleInitial(request.getMiddleInitial()); // UC-30 fix: save middleInitial
        instructor.setLastName(request.getLastName());
        instructor.setEmail(invitationToken.getEmail());
        instructor.setStatus(InstructorStatus.ACTIVE);

        Instructor saved = instructorRepository.save(instructor);
        invitationToken.setUsed(true);
        invitationTokenRepository.save(invitationToken);

        return toResponse(saved);
    }

    // UC-31: Peer eval report for entire section for a given week.
    // Includes students who DID NOT submit (submittedEval=false).
    // Sorts by lastName ascending per spec.
    @Transactional(readOnly = true)
    public List<PeerEvalReportRow> generateSectionPeerEvalReport(Long sectionId, String activeWeek) {
        List<PeerEvaluation> evals = peerEvaluationRepository.findBySectionIdAndActiveWeek(sectionId, activeWeek);

        // Group submitted evals by student
        Map<Long, List<PeerEvaluation>> byStudent = evals.stream()
                .collect(Collectors.groupingBy(pe -> pe.getStudent().getId()));

        List<PeerEvalReportRow> rows = new ArrayList<>();

        // Rows for students who submitted
        for (Map.Entry<Long, List<PeerEvaluation>> entry : byStudent.entrySet()) {
            List<PeerEvaluation> studentEvals = entry.getValue();
            PeerEvaluation first = studentEvals.get(0);
            Student student = first.getStudent();

            PeerEvalReportRow row = new PeerEvalReportRow();
            row.setStudentName(student.getFirstName() + " " + student.getLastName());
            row.setLastName(student.getLastName());

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

        // Rows for students who did NOT submit — find via section
        // We use the students found in submitted evals' teams as approximation.
        // If a student's ID is not in byStudent, they didn't submit.
        Set<Long> submittedIds = byStudent.keySet();
        if (!evals.isEmpty()) {
            // Collect all students in the same teams as any submitter
            Set<Student> sectionStudents = evals.stream()
                    .flatMap(pe -> pe.getStudent().getTeams().stream())
                    .flatMap(team -> team.getStudents().stream())
                    .collect(Collectors.toSet());

            for (Student s : sectionStudents) {
                if (!submittedIds.contains(s.getId())) {
                    PeerEvalReportRow row = new PeerEvalReportRow();
                    row.setStudentName(s.getFirstName() + " " + s.getLastName());
                    row.setLastName(s.getLastName());
                    row.setSubmittedEval(false);
                    row.setPublicComments(new ArrayList<>());
                    rows.add(row);
                }
            }
        }

        rows.sort(Comparator.comparing(PeerEvalReportRow::getLastName));
        return rows;
    }

    // UC-32: WAR report for a team for a given week.
    // Includes students who DID NOT submit a WAR (submittedWar=false).
    // Sorts by lastName ascending per spec.
    @Transactional(readOnly = true)
    public List<WarReportRow> generateTeamWarReport(Long teamId, String activeWeek) {
        LocalDate weekStart = LocalDate.parse(activeWeek, WEEK_FMT);
        List<War> wars = warRepository.findByTeamIdAndWeekStart(teamId, weekStart);

        // Students who submitted
        Set<Long> submittedIds = wars.stream()
                .map(w -> w.getStudent().getId())
                .collect(Collectors.toSet());

        List<WarReportRow> rows = new ArrayList<>();

        for (War war : wars) {
            Student student = war.getStudent();
            String weekStartStr = war.getWeekStart().format(WEEK_FMT);
            String weekEndStr   = war.getWeekEnd().format(WEEK_FMT);

            for (Activity activity : war.getActivities()) {
                WarReportRow row = new WarReportRow();
                row.setStudentName(student.getFirstName() + " " + student.getLastName());
                row.setLastName(student.getLastName());
                row.setWeekStart(weekStartStr);
                row.setWeekEnd(weekEndStr);
                row.setActivityCategory(activity.getCategory().name());
                row.setDescription(activity.getDescription());
                row.setPlannedHours(activity.getPlannedHours());
                row.setActualHours(activity.getActualHours());
                row.setStatus(activity.getStatus().name());
                row.setSubmittedWar(true);
                rows.add(row);
            }
        }

        // Students who did NOT submit — look up all team members
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
            for (Student s : team.getStudents()) {
                if (!submittedIds.contains(s.getId())) {
                    WarReportRow row = new WarReportRow();
                    row.setStudentName(s.getFirstName() + " " + s.getLastName());
                    row.setLastName(s.getLastName());
                    row.setWeekStart(activeWeek);
                    row.setSubmittedWar(false);
                    rows.add(row);
                }
            }
        }

        rows.sort(Comparator.comparing(WarReportRow::getLastName));
        return rows;
    }

    // UC-33: Peer eval report for a student over a date range
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

    // UC-34: WAR report for a student over a date range
    @Transactional(readOnly = true)
    public List<WarReportRow> generateStudentWarReport(Long studentId,
                                                        String startWeek,
                                                        String endWeek) {
        LocalDate startDate = LocalDate.parse(startWeek, WEEK_FMT);
        LocalDate endDate   = LocalDate.parse(endWeek,   WEEK_FMT);
        List<War> wars = warRepository.findByStudentIdAndWeekRange(studentId, startDate, endDate);

        List<WarReportRow> rows = new ArrayList<>();
        for (War war : wars) {
            String weekStartStr = war.getWeekStart().format(WEEK_FMT);
            String weekEndStr   = war.getWeekEnd().format(WEEK_FMT);

            for (Activity activity : war.getActivities()) {
                WarReportRow row = new WarReportRow();
                row.setWeekStart(weekStartStr);
                row.setWeekEnd(weekEndStr);
                row.setActivityCategory(activity.getCategory().name());
                row.setDescription(activity.getDescription());
                row.setPlannedHours(activity.getPlannedHours());
                row.setActualHours(activity.getActualHours());
                row.setStatus(activity.getStatus().name());
                row.setSubmittedWar(true);
                rows.add(row);
            }
        }

        rows.sort(Comparator.comparing(WarReportRow::getWeekStart));
        return rows;
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    private InstructorSummaryDto toSummaryDto(Instructor instructor) {
        List<String> teamNames = instructor.getTeams().stream().map(Team::getName).toList();
        return new InstructorSummaryDto(instructor.getId(), instructor.getFirstName(),
                instructor.getLastName(), teamNames, instructor.getStatus());
    }

    private InstructorDetailDto toDetailDto(Instructor instructor) {
        Map<String, List<String>> bySection = instructor.getTeams().stream()
                .collect(Collectors.groupingBy(
                        team -> team.getSection() != null ? team.getSection().getName() : "No Section",
                        Collectors.mapping(Team::getName, Collectors.toList())
                ));
        return new InstructorDetailDto(instructor.getId(), instructor.getFirstName(),
                instructor.getLastName(), instructor.getEmail(), instructor.getStatus(), bySection);
    }

    private InstructorResponse toResponse(Instructor instructor) {
        InstructorResponse response = new InstructorResponse();
        response.setId(instructor.getId());
        response.setFirstName(instructor.getFirstName());
        response.setMiddleInitial(instructor.getMiddleInitial());
        response.setLastName(instructor.getLastName());
        response.setEmail(instructor.getEmail());
        response.setStatus(instructor.getStatus());
        if (instructor.getTeams() != null)
            response.setTeamNames(instructor.getTeams().stream().map(Team::getName).toList());
        return response;
    }
}