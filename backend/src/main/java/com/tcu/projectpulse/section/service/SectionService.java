package com.tcu.projectpulse.section.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.rubric.domain.Rubric;
import com.tcu.projectpulse.rubric.repository.RubricRepository;
import com.tcu.projectpulse.section.domain.ActiveWeek;
import com.tcu.projectpulse.section.domain.Section;
import com.tcu.projectpulse.section.dto.*;
import com.tcu.projectpulse.section.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;

    public SectionService(SectionRepository sectionRepository, RubricRepository rubricRepository) {
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
    }

    // UC-2: Find sections
    @Transactional(readOnly = true)
    public List<SectionDto> findSections(String name) {
        List<Section> sections;
        if (name != null && !name.isEmpty()) {
            sections = sectionRepository.findByNameContainingIgnoreCaseOrderByNameDesc(name);
        } else {
            sections = sectionRepository.findAll();
        }
        return sections.stream().map(this::toDto).toList();
    }

    // UC-3: View a section
    @Transactional(readOnly = true)
    public SectionDto findById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Section", id));
        return toDto(section);
    }

    // UC-4: Create a section
    public SectionDto createSection(SectionRequest request) {
        if (sectionRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Section '" + request.name() + "' already exists.");
        }
        Section section = new Section();
        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        if (request.rubricId() != null) {
            Rubric rubric = rubricRepository.findById(request.rubricId())
                    .orElseThrow(() -> new ObjectNotFoundException("Rubric", request.rubricId()));
            section.setRubric(rubric);
        }
        return toDto(sectionRepository.save(section));
    }

    // UC-5: Edit a section
    public SectionDto updateSection(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Section", id));
        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        if (request.rubricId() != null) {
            Rubric rubric = rubricRepository.findById(request.rubricId())
                    .orElseThrow(() -> new ObjectNotFoundException("Rubric", request.rubricId()));
            section.setRubric(rubric);
        }
        return toDto(sectionRepository.save(section));
    }

    // UC-6: Set up active weeks
    public List<ActiveWeekDto> setupActiveWeeks(Long sectionId, ActiveWeekRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Section", sectionId));

        section.getActiveWeeks().clear();

        List<ActiveWeek> weeks = new ArrayList<>();
        LocalDate current = section.getStartDate();
        while (!current.isAfter(section.getEndDate())) {
            ActiveWeek week = new ActiveWeek();
            week.setStartDate(current);
            week.setEndDate(current.plusDays(6));
            week.setSection(section);
            boolean inactive = request.inactiveWeekStarts() != null &&
                    request.inactiveWeekStarts().contains(current);
            week.setIsActive(!inactive);
            weeks.add(week);
            current = current.plusWeeks(1);
        }
        section.getActiveWeeks().addAll(weeks);
        sectionRepository.save(section);
        return weeks.stream()
                .map(w -> new ActiveWeekDto(w.getId(), w.getStartDate(), w.getEndDate(), w.getIsActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActiveWeekDto> getActiveWeeks(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Section", sectionId));
        return section.getActiveWeeks().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()))
                .sorted(java.util.Comparator.comparing(ActiveWeek::getStartDate))
                .map(w -> new ActiveWeekDto(w.getId(), w.getStartDate(), w.getEndDate(), w.getIsActive()))
                .toList();
    }

    public void deleteSection(Long id) {
        sectionRepository.deleteById(id);
    }

    private SectionDto toDto(Section section) {
        List<String> teamNames = section.getTeams().stream()
                .map(t -> t.getName())
                .toList();
        String rubricName = section.getRubric() != null ? section.getRubric().getName() : null;
        return new SectionDto(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                rubricName,
                teamNames
        );
    }
}