package com.tcu.projectpulse.rubric.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.rubric.domain.Rubric;
import com.tcu.projectpulse.rubric.domain.RubricCriterion;
import com.tcu.projectpulse.rubric.dto.*;
import com.tcu.projectpulse.rubric.repository.RubricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RubricService {

    private final RubricRepository rubricRepository;

    public RubricService(RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    public List<RubricDto> getAllRubrics() {
        return rubricRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public RubricDto findById(Long id) {
        Rubric rubric = rubricRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Rubric", id));
        return toDto(rubric);
    }

    public RubricDto createRubric(RubricRequest request) {
        if (rubricRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Rubric with name '" + request.name() + "' already exists.");
        }
        Rubric rubric = new Rubric();
        rubric.setName(request.name());
        if (request.criteria() != null) {
            for (RubricCriterionDto dto : request.criteria()) {
                RubricCriterion criterion = new RubricCriterion();
                criterion.setName(dto.name());
                criterion.setDescription(dto.description());
                criterion.setMaxScore(dto.maxScore());
                criterion.setRubric(rubric);
                rubric.getCriteria().add(criterion);
            }
        }
        return toDto(rubricRepository.save(rubric));
    }

    public RubricDto duplicateAndEdit(Long originalId, RubricRequest request) {
        rubricRepository.findById(originalId)
                .orElseThrow(() -> new ObjectNotFoundException("Rubric", originalId));
        return createRubric(request);
    }

    public void deleteRubric(Long id) {
        rubricRepository.deleteById(id);
    }

    private RubricDto toDto(Rubric rubric) {
        List<RubricCriterionDto> criteria = rubric.getCriteria().stream()
                .map(c -> new RubricCriterionDto(c.getId(), c.getName(), c.getDescription(), c.getMaxScore()))
                .toList();
        return new RubricDto(rubric.getId(), rubric.getName(), criteria);
    }
}