package com.tcu.projectpulse.war;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.student.domain.Student;
import com.tcu.projectpulse.student.repository.StudentRepository;
import com.tcu.projectpulse.war.domain.Activity;
import com.tcu.projectpulse.war.domain.ActivityCategory;
import com.tcu.projectpulse.war.domain.ActivityStatus;
import com.tcu.projectpulse.war.domain.War;
import com.tcu.projectpulse.war.dto.ActivityRequest;
import com.tcu.projectpulse.war.dto.ActivityResponse;
import com.tcu.projectpulse.war.dto.WarResponse;
import com.tcu.projectpulse.war.repository.ActivityRepository;
import com.tcu.projectpulse.war.repository.WarRepository;
import com.tcu.projectpulse.war.service.WarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WarServiceTest {

    @Mock
    private WarRepository warRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private WarService warService;

    private Student student;
    private War war;
    private Activity activity;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane@example.com");

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        war = new War();
        war.setId(1L);
        war.setStudent(student);
        war.setWeekStart(monday);
        war.setWeekEnd(monday.plusDays(6));

        activity = new Activity();
        activity.setId(10L);
        activity.setWar(war);
        activity.setCategory(ActivityCategory.DEVELOPMENT);
        activity.setDescription("Implement feature");
        activity.setPlannedHours(2.0);
        activity.setActualHours(1.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);

        war.getActivities().add(activity);
    }

    @Test
    void getOrCreateWar_existingWar_returnsWarWithActivities() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        given(warRepository.findByStudentIdAndWeekStart(1L, monday)).willReturn(Optional.of(war));

        WarResponse result = warService.getOrCreateWar(1L, monday);

        assertThat(result.weekStart()).isEqualTo(monday);
        assertThat(result.activities()).hasSize(1);
        assertThat(result.activities().get(0).category()).isEqualTo("DEVELOPMENT");
    }

    @Test
    void getOrCreateWar_futureWeek_throwsIllegalArgumentException() {
        LocalDate futureMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);

        assertThatThrownBy(() -> warService.getOrCreateWar(1L, futureMonday))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future week");
    }

    @Test
    void addActivity_validRequest_savesAndReturnsActivityResponse() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));

        Activity saved = new Activity();
        saved.setId(20L);
        saved.setWar(war);
        saved.setCategory(ActivityCategory.TESTING);
        saved.setDescription("Write tests");
        saved.setPlannedHours(3.0);
        saved.setActualHours(0.0);
        saved.setStatus(ActivityStatus.IN_PROGRESS);
        given(activityRepository.save(any(Activity.class))).willReturn(saved);

        ActivityResponse result = warService.addActivity(1L, 1L,
                new ActivityRequest("TESTING", "Write tests", 3.0, 0.0, "IN_PROGRESS"));

        assertThat(result.category()).isEqualTo("TESTING");
        assertThat(result.description()).isEqualTo("Write tests");
        assertThat(result.plannedHours()).isEqualTo(3.0);
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void addActivity_invalidCategory_throwsIllegalArgumentException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));

        assertThatThrownBy(() -> warService.addActivity(1L, 1L,
                new ActivityRequest("BADCATEGORY", "desc", 2.0, 0.0, "IN_PROGRESS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid category");
    }

    @Test
    void addActivity_invalidStatus_throwsIllegalArgumentException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));

        assertThatThrownBy(() -> warService.addActivity(1L, 1L,
                new ActivityRequest("DEVELOPMENT", "desc", 2.0, 0.0, "BADSTATUS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    void addActivity_negativePlannedHours_throwsIllegalArgumentException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));

        assertThatThrownBy(() -> warService.addActivity(1L, 1L,
                new ActivityRequest("DEVELOPMENT", "desc", -1.0, 0.0, "IN_PROGRESS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Planned hours");
    }

    @Test
    void addActivity_negativeActualHours_throwsIllegalArgumentException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));

        assertThatThrownBy(() -> warService.addActivity(1L, 1L,
                new ActivityRequest("DEVELOPMENT", "desc", 2.0, -1.0, "IN_PROGRESS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Actual hours");
    }

    @Test
    void updateActivity_validRequest_returnsUpdatedActivity() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));
        given(activityRepository.findById(10L)).willReturn(Optional.of(activity));

        ActivityResponse result = warService.updateActivity(1L, 10L, 1L,
                new ActivityRequest("TESTING", "Updated desc", 4.0, 2.0, "DONE"));

        assertThat(result.category()).isEqualTo("TESTING");
        assertThat(result.description()).isEqualTo("Updated desc");
        assertThat(result.actualHours()).isEqualTo(2.0);
        assertThat(result.status()).isEqualTo("DONE");
    }

    @Test
    void updateActivity_activityNotFound_throwsObjectNotFoundException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));
        given(activityRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> warService.updateActivity(1L, 99L, 1L,
                new ActivityRequest("DEVELOPMENT", "desc", 2.0, 0.0, "IN_PROGRESS")))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteActivity_existingActivity_deletesSuccessfully() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));
        given(activityRepository.findById(10L)).willReturn(Optional.of(activity));

        warService.deleteActivity(1L, 10L, 1L);

        verify(activityRepository).delete(activity);
    }

    @Test
    void deleteActivity_activityNotFound_throwsObjectNotFoundException() {
        given(warRepository.findById(1L)).willReturn(Optional.of(war));
        given(activityRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> warService.deleteActivity(1L, 99L, 1L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
