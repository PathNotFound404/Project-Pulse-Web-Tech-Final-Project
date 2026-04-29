package com.tcu.projectpulse.section.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.section.dto.*;
import com.tcu.projectpulse.section.service.SectionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
@CrossOrigin(origins = "http://localhost:5173")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    // UC-2: Find sections
    @GetMapping
    public Result findSections(@RequestParam(required = false) String name) {
        List<SectionDto> sections = sectionService.findSections(name);
        return Result.success("Success", sections);
    }

    // UC-3: View a section
    @GetMapping("/{id}")
    public Result getSection(@PathVariable Long id) {
        SectionDto section = sectionService.findById(id);
        return Result.success("Success", section);
    }

    // UC-4: Create a section
    @PostMapping
    public Result createSection(@RequestBody SectionRequest request) {
        SectionDto section = sectionService.createSection(request);
        return Result.success("Section created", section);
    }

    // UC-5: Edit a section
    @PutMapping("/{id}")
    public Result updateSection(@PathVariable Long id, @RequestBody SectionRequest request) {
        SectionDto section = sectionService.updateSection(id, request);
        return Result.success("Section updated", section);
    }

    // UC-6: Get active weeks for a section
    @GetMapping("/{id}/active-weeks")
    public Result getActiveWeeks(@PathVariable Long id) {
        List<ActiveWeekDto> weeks = sectionService.getActiveWeeks(id);
        return Result.success("Success", weeks);
    }

    // UC-6: Set up active weeks
    @PostMapping("/{id}/active-weeks")
    public Result setupActiveWeeks(@PathVariable Long id, @RequestBody ActiveWeekRequest request) {
        List<ActiveWeekDto> weeks = sectionService.setupActiveWeeks(id, request);
        return Result.success("Active weeks set up", weeks);
    }

    // Delete a section
    @DeleteMapping("/{id}")
    public Result deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return Result.success("Section deleted", null);
    }
}