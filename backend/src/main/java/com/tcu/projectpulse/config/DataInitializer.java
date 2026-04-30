package com.tcu.projectpulse.config;

import com.tcu.projectpulse.admin.domain.Admin;
import com.tcu.projectpulse.admin.repository.AdminRepository;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
import com.tcu.projectpulse.section.domain.ActiveWeek;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.section.repository.SectionRepository;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.team.domain.Team;
import com.tcu.projectpulse.team.repository.TeamRepository;
import com.tcu.projectpulse.war.domain.Activity;
import com.tcu.projectpulse.war.domain.ActivityCategory;
import com.tcu.projectpulse.war.domain.ActivityStatus;
import com.tcu.projectpulse.war.domain.War;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final String SEED_PASSWORD_HASH = encoder.encode("password123");
    private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final AdminRepository adminRepository;

    public DataInitializer(SectionRepository sectionRepository,
                           TeamRepository teamRepository,
                           StudentRepository studentRepository,
                           InstructorRepository instructorRepository,
                           InvitationTokenRepository invitationTokenRepository,
                           AdminRepository adminRepository) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (adminRepository.count() > 0) {
            System.out.println("=== DataInitializer: seed data already present, skipping ===");
            return;
        }

        // Use the current Monday as an anchor so seed data is always "this semester"
        LocalDate thisMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        // --- Admin ---
        adminRepository.save(admin("Admin", "User", "admin@tcu.edu"));

        // --- Sections ---
        // 2024-2025: spans ±8 weeks from today so it always covers the current week
        Section section2425 = new Section("2024-2025");
        section2425.setStartDate(thisMonday.minusWeeks(8));
        section2425.setEndDate(thisMonday.plusWeeks(8));
        section2425 = sectionRepository.save(section2425);
        addActiveWeeks(section2425);
        sectionRepository.save(section2425);

        // 2023-2024: historical section
        Section section2324 = new Section("2023-2024");
        section2324.setStartDate(LocalDate.of(2024, 1, 8));
        section2324.setEndDate(LocalDate.of(2024, 4, 29));
        section2324 = sectionRepository.save(section2324);

        // --- Instructors ---
        Instructor drSmith   = instructorRepository.save(instructor("Alice",  "Smith",   "a.smith@tcu.edu",   InstructorStatus.ACTIVE));
        Instructor drJohnson = instructorRepository.save(instructor("Robert", "Johnson", "r.johnson@tcu.edu", InstructorStatus.ACTIVE));
        Instructor drLee     = instructorRepository.save(instructor("Linda",  "Lee",     "l.lee@tcu.edu",     InstructorStatus.ACTIVE));
        Instructor drBrown   = instructorRepository.save(instructor("Mark",   "Brown",   "m.brown@tcu.edu",   InstructorStatus.DEACTIVATED));

        // --- Students (2024-2025) ---
        Student alice = studentRepository.save(student("Alice", "Adams", "a.adams@tcu.edu", section2425));
        Student bob   = studentRepository.save(student("Bob",   "Baker", "b.baker@tcu.edu", section2425));
        Student carol = studentRepository.save(student("Carol", "Clark", "c.clark@tcu.edu", section2425));
        Student david = studentRepository.save(student("David", "Davis", "d.davis@tcu.edu", section2425));
        Student eve   = studentRepository.save(student("Eve",   "Evans", "e.evans@tcu.edu", section2425));
        Student frank = studentRepository.save(student("Frank", "Foster","f.foster@tcu.edu",section2425));

        // --- Students (2023-2024) ---
        Student grace = studentRepository.save(student("Grace", "Green",  "g.green@tcu.edu",  section2324));
        Student henry = studentRepository.save(student("Henry", "Harris", "h.harris@tcu.edu", section2324));

        // Seed WARs using recent weeks so reports show data immediately
        addWars(alice, 3, thisMonday.minusWeeks(3));
        studentRepository.save(alice);

        // --- Teams (2024-2025) ---
        Team teamAlpha = new Team();
        teamAlpha.setName("Team Alpha");
        teamAlpha.setSection(section2425);
        teamAlpha.setStudents(new ArrayList<>(List.of(alice, bob, carol)));
        teamAlpha.setInstructors(new ArrayList<>(List.of(drSmith)));
        teamRepository.save(teamAlpha);

        Team teamBeta = new Team();
        teamBeta.setName("Team Beta");
        teamBeta.setSection(section2425);
        teamBeta.setStudents(new ArrayList<>(List.of(david, eve, frank)));
        teamBeta.setInstructors(new ArrayList<>(List.of(drJohnson, drLee)));
        teamRepository.save(teamBeta);

        // --- Teams (2023-2024) ---
        Team teamGamma = new Team();
        teamGamma.setName("Team Gamma");
        teamGamma.setSection(section2324);
        teamGamma.setStudents(new ArrayList<>(List.of(grace, henry)));
        teamGamma.setInstructors(new ArrayList<>(List.of(drSmith)));
        teamRepository.save(teamGamma);

        // Team Delta — no students or instructors assigned yet
        Team teamDelta = new Team();
        teamDelta.setName("Team Delta");
        teamDelta.setSection(section2425);
        teamDelta.setStudents(new ArrayList<>());
        teamDelta.setInstructors(new ArrayList<>());
        teamRepository.save(teamDelta);

        // --- Invitation Token for UC-30 testing ---
        InvitationToken testToken = new InvitationToken("test-token-123", "eduarda@tcu.edu");
        testToken.setUsed(false);
        invitationTokenRepository.save(testToken);

        System.out.println("=== DataInitializer: seed data loaded ===");
        System.out.println("Admin       : admin@tcu.edu / password123");
        System.out.println("Instructors : a.smith@tcu.edu / r.johnson@tcu.edu / l.lee@tcu.edu (all password123)");
        System.out.println("Students    : a.adams@tcu.edu … f.foster@tcu.edu (all password123)");
        System.out.println("Section 2024-2025 has active weeks from " + section2425.getStartDate() + " to " + section2425.getEndDate());
        System.out.println("WAR data    : Team Alpha / Alice Adams — weeks " + thisMonday.minusWeeks(3) + " through " + thisMonday.minusWeeks(1));
        System.out.println("Peer eval   : Alice Adams (section 2024-2025) — weeks " + thisMonday.minusWeeks(2).format(WEEK_FMT) + " and " + thisMonday.minusWeeks(1).format(WEEK_FMT));
        System.out.println("Invite token: test-token-123  →  eduarda@tcu.edu");
        System.out.println("=========================================");
    }

    private void addActiveWeeks(Section section) {
        LocalDate cursor = section.getStartDate();
        while (!cursor.isAfter(section.getEndDate())) {
            ActiveWeek aw = new ActiveWeek();
            aw.setStartDate(cursor);
            aw.setEndDate(cursor.plusDays(6));
            aw.setIsActive(true);
            aw.setSection(section);
            section.getActiveWeeks().add(aw);
            cursor = cursor.plusWeeks(1);
        }
    }

    private Admin admin(String firstName, String lastName, String email) {
        Admin a = new Admin();
        a.setFirstName(firstName);
        a.setLastName(lastName);
        a.setEmail(email);
        a.setPasswordHash(SEED_PASSWORD_HASH);
        return a;
    }

    private Instructor instructor(String firstName, String lastName, String email, InstructorStatus status) {
        Instructor i = new Instructor();
        i.setFirstName(firstName);
        i.setLastName(lastName);
        i.setEmail(email);
        i.setStatus(status);
        i.setPassword(SEED_PASSWORD_HASH);
        i.setTeams(new ArrayList<>());
        return i;
    }

    private Student student(String firstName, String lastName, String email, Section section) {
        Student s = new Student();
        s.setFirstName(firstName);
        s.setLastName(lastName);
        s.setEmail(email);
        s.setSection(section);
        s.setPasswordHash(SEED_PASSWORD_HASH);
        return s;
    }

    private void addWars(Student student, int count, LocalDate firstWeekStart) {
        List<War> wars = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDate weekStart = firstWeekStart.plusWeeks(i);
            LocalDate weekEnd   = weekStart.plusDays(6);

            War war = new War();
            war.setStudent(student);
            war.setWeekStart(weekStart);
            war.setWeekEnd(weekEnd);

            Activity dev = new Activity();
            dev.setWar(war);
            dev.setCategory(ActivityCategory.DEVELOPMENT);
            dev.setDescription("Write unit tests for sprint " + (i + 1));
            dev.setPlannedHours(5.0);
            dev.setActualHours(4.5);
            dev.setStatus(ActivityStatus.DONE);

            Activity review = new Activity();
            review.setWar(war);
            review.setCategory(ActivityCategory.COMMUNICATION);
            review.setDescription("Sprint " + (i + 1) + " review meeting");
            review.setPlannedHours(1.0);
            review.setActualHours(1.0);
            review.setStatus(ActivityStatus.DONE);

            war.setActivities(new ArrayList<>(List.of(dev, review)));
            wars.add(war);
        }
        student.setWars(wars);
    }

    private void addPeerEvaluations(Student student, Student evaluator, int count, LocalDate firstWeekStart) {
        List<PeerEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDate weekStart = firstWeekStart.plusWeeks(i);
            PeerEvaluation pe = new PeerEvaluation();
            pe.setStudent(student);
            pe.setEvaluator(evaluator);
            pe.setWeekStart(weekStart);
            pe.setWeekEnd(weekStart.plusDays(6));
            // activeWeek stored as MM-dd-yyyy start date to match what the frontend sends for UC-31
            pe.setActiveWeek(weekStart.format(WEEK_FMT));
            pe.setCourtesy(4);
            pe.setEngagementInMeetings(4);
            pe.setInitiative(3);
            pe.setOpenMindedness(5);
            pe.setProductivity(4);
            pe.setQualityOfWork(4);
            pe.setPublicComments("Great team member.");
            pe.setPrivateComments("Could improve communication.");
            evals.add(pe);
        }
        student.setPeerEvaluations(evals);
    }
}
