package com.tcu.projectpulse.section;

import com.tcu.projectpulse.rubric.domain.Rubric;
import com.tcu.projectpulse.rubric.repository.RubricRepository;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.section.dto.SectionDto;
import com.tcu.projectpulse.section.dto.SectionRequest;
import com.tcu.projectpulse.section.repository.SectionRepository;
import com.tcu.projectpulse.section.service.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private RubricRepository rubricRepository;

    @InjectMocks
    private SectionService sectionService;

    private Section section;
    private Rubric rubric;

    @BeforeEach
    void setUp() {
        rubric = new Rubric();
        rubric.setId(1L);
        rubric.setName("Peer Eval Rubric v1");

        section = new Section();
        section.setId(1L);
        section.setName("Section 2024-2025");
        section.setStartDate(LocalDate.of(2024, 8, 21));
        section.setEndDate(LocalDate.of(2025, 5, 1));
        section.setRubric(rubric);
        section.setTeams(List.of());
        section.setActiveWeeks(List.of());
    }

    // UC-2: Find sections
    @Test
    void findSections_noFilter_returnsAll() {
        when(sectionRepository.findAll()).thenReturn(List.of(section));

        List<SectionDto> result = sectionService.findSections(null);

        assertEquals(1, result.size());
        assertEquals("Section 2024-2025", result.get(0).name());
    }

    // UC-3: View a section
    @Test
    void findById_success() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

        SectionDto result = sectionService.findById(1L);

        assertNotNull(result);
        assertEquals("Section 2024-2025", result.name());
    }

    // UC-3: View section not found
    @Test
    void findById_notFound_throwsException() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> sectionService.findById(99L));
    }

    // UC-4: Create a section
    @Test
    void createSection_success() {
        SectionRequest request = new SectionRequest(
                "Section 2025-2026",
                LocalDate.of(2025, 8, 21),
                LocalDate.of(2026, 5, 1),
                1L
        );
        when(sectionRepository.existsByName("Section 2025-2026")).thenReturn(false);
        when(rubricRepository.findById(1L)).thenReturn(Optional.of(rubric));
        when(sectionRepository.save(any(Section.class))).thenReturn(section);

        SectionDto result = sectionService.createSection(request);

        assertNotNull(result);
        verify(sectionRepository, times(1)).save(any(Section.class));
    }

    // UC-4: Create section with duplicate name fails
    @Test
    void createSection_duplicateName_throwsException() {
        SectionRequest request = new SectionRequest(
                "Section 2024-2025",
                LocalDate.of(2024, 8, 21),
                LocalDate.of(2025, 5, 1),
                1L
        );
        when(sectionRepository.existsByName("Section 2024-2025")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> sectionService.createSection(request));
        verify(sectionRepository, never()).save(any());
    }

    // UC-5: Edit a section
    @Test
    void updateSection_success() {
        SectionRequest request = new SectionRequest(
                "Section 2024-2025 Updated",
                LocalDate.of(2024, 8, 21),
                LocalDate.of(2025, 5, 1),
                1L
        );
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(rubricRepository.findById(1L)).thenReturn(Optional.of(rubric));
        when(sectionRepository.save(any(Section.class))).thenReturn(section);

        SectionDto result = sectionService.updateSection(1L, request);

        assertNotNull(result);
        verify(sectionRepository, times(1)).save(any(Section.class));
    }
}