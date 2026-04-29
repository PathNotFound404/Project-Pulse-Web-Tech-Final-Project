package com.tcu.projectpulse.instructor.service;

import com.tcu.projectpulse.common.exception.ObjectNotFoundException;
import com.tcu.projectpulse.instructor.domain.Instructor;
import com.tcu.projectpulse.instructor.domain.InstructorStatus;
import com.tcu.projectpulse.instructor.domain.InvitationToken;
import com.tcu.projectpulse.instructor.dto.*;
import com.tcu.projectpulse.instructor.repository.InstructorRepository;
import com.tcu.projectpulse.instructor.repository.InvitationTokenRepository;
import com.tcu.projectpulse.team.domain.Team;
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

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public InstructorService(InstructorRepository instructorRepository,
                             InvitationTokenRepository invitationTokenRepository) {
        this.instructorRepository = instructorRepository;
        this.invitationTokenRepository = invitationTokenRepository;
    }

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

            String link = frontendBaseUrl + "/register?token=" + token;
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
}
