package com.tcu.projectpulse.config;

import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.peerevaluation.domain.PeerEvaluation;
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

    public DataInitializer(SectionRepository sectionRepository,
                           TeamRepository teamRepository,
                           StudentRepository studentRepository,
                           InstructorRepository instructorRepository) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // --- Sections ---
        Section section2425 = sectionRepository.save(section("2024-2025"));
        Section section2324 = sectionRepository.save(section("2023-2024"));

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

        // Add WARs and PeerEvaluations to Alice so UC-16 / UC-32 / UC-33 / UC-34 show data
        addWars(alice, 3);
        addPeerEvaluations(alice, bob, 2);
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

        System.out.println("=== DataInitializer: seed data loaded ===");
        System.out.println("Sections : 2024-2025 (id=" + section2425.getId() + "), 2023-2024 (id=" + section2324.getId() + ")");
        System.out.println("Instructors: Smith(ACTIVE), Johnson(ACTIVE), Lee(ACTIVE), Brown(DEACTIVATED)");
        System.out.println("Teams     : Alpha, Beta, Gamma, Delta");
        System.out.println("Students  : Alice, Bob, Carol, David, Eve, Frank (2024-2025) | Grace, Henry (2023-2024)");
        System.out.println("=========================================");
    }

    private Section section(String name) {
        return new Section(name);
    }

    private Instructor instructor(String firstName, String lastName, String email, InstructorStatus status) {
        Instructor i = new Instructor();
        i.setFirstName(firstName);
        i.setLastName(lastName);
        i.setEmail(email);
        i.setStatus(status);
        i.setPasswordHash(SEED_PASSWORD_HASH);
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

    private void addWars(Student student, int count) {
        List<War> wars = new ArrayList<>();
        LocalDate monday = LocalDate.of(2025, 1, 6);

        for (int i = 0; i < count; i++) {
            LocalDate weekStart = monday.plusWeeks(i);
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

    private void addPeerEvaluations(Student student, Student evaluator, int count) {
        List<PeerEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PeerEvaluation pe = new PeerEvaluation();
            pe.setStudent(student);
            pe.setEvaluator(evaluator);
            LocalDate peWeekStart = LocalDate.of(2025, 1, 6).plusWeeks(i);
            LocalDate peWeekEnd = peWeekStart.plusDays(6);
            pe.setActiveWeek(peWeekStart.format(WEEK_FMT) + " to " + peWeekEnd.format(WEEK_FMT));
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