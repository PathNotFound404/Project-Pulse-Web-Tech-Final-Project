package com.tcu.projectpulse.rubric;

import com.tcu.projectpulse.rubric.domain.Rubric;
import com.tcu.projectpulse.rubric.dto.RubricCriterionDto;
import com.tcu.projectpulse.rubric.dto.RubricDto;
import com.tcu.projectpulse.rubric.dto.RubricRequest;
import com.tcu.projectpulse.rubric.repository.RubricRepository;
import com.tcu.projectpulse.rubric.service.RubricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RubricServiceTest {

    @Mock
    private RubricRepository rubricRepository;

    @InjectMocks
    private RubricService rubricService;

    private Rubric rubric;

    @BeforeEach
    void setUp() {
        rubric = new Rubric();
        rubric.setId(1L);
        rubric.setName("Peer Eval Rubric v1");
        rubric.setCriteria(List.of());
    }

    // UC-1: Create a rubric
    @Test
    void createRubric_success() {
        RubricRequest request = new RubricRequest("Peer Eval Rubric v1", List.of(
                new RubricCriterionDto(null, "Quality of work", "How do you rate quality?", 10.0)
        ));
        when(rubricRepository.existsByName("Peer Eval Rubric v1")).thenReturn(false);
        when(rubricRepository.save(any(Rubric.class))).thenReturn(rubric);

        RubricDto result = rubricService.createRubric(request);

        assertNotNull(result);
        assertEquals("Peer Eval Rubric v1", result.name());
        verify(rubricRepository, times(1)).save(any(Rubric.class));
    }

    // UC-1: Create rubric with duplicate name fails
    @Test
    void createRubric_duplicateName_throwsException() {
        RubricRequest request = new RubricRequest("Peer Eval Rubric v1", List.of());
        when(rubricRepository.existsByName("Peer Eval Rubric v1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> rubricService.createRubric(request));
        verify(rubricRepository, never()).save(any());
    }

    // UC-1: Get all rubrics
    @Test
    void getAllRubrics_success() {
        when(rubricRepository.findAll()).thenReturn(List.of(rubric));

        List<RubricDto> result = rubricService.getAllRubrics();

        assertEquals(1, result.size());
        assertEquals("Peer Eval Rubric v1", result.get(0).name());
    }

    // UC-1: Get rubric by id
    @Test
    void findById_success() {
        when(rubricRepository.findById(1L)).thenReturn(Optional.of(rubric));

        RubricDto result = rubricService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    // UC-1: Get rubric by id not found
    @Test
    void findById_notFound_throwsException() {
        when(rubricRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> rubricService.findById(99L));
    }
}