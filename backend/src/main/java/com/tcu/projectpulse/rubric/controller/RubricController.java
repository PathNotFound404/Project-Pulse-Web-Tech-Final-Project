package com.tcu.projectpulse.rubric.controller;

import com.tcu.projectpulse.common.dto.Result;
import com.tcu.projectpulse.rubric.dto.*;
import com.tcu.projectpulse.rubric.service.RubricService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rubrics")
@CrossOrigin(origins = "http://localhost:5173")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    // UC-1: Get all rubrics
    @GetMapping
    public Result getAllRubrics() {
        List<RubricDto> rubrics = rubricService.getAllRubrics();
        return Result.success("Success", rubrics);
    }

    // UC-1: Get rubric by id
    @GetMapping("/{id}")
    public Result getRubric(@PathVariable Long id) {
        RubricDto rubric = rubricService.findById(id);
        return Result.success("Success", rubric);
    }

    // UC-1: Create a rubric
    @PostMapping
    public Result createRubric(@RequestBody RubricRequest request) {
        RubricDto rubric = rubricService.createRubric(request);
        return Result.success("Rubric created", rubric);
    }

    // UC-4: Duplicate and edit rubric for a section
    @PostMapping("/{id}/duplicate")
    public Result duplicateRubric(@PathVariable Long id, @RequestBody RubricRequest request) {
        RubricDto rubric = rubricService.duplicateAndEdit(id, request);
        return Result.success("Rubric duplicated", rubric);
    }

    // UC-1: Delete a rubric
    @DeleteMapping("/{id}")
    public Result deleteRubric(@PathVariable Long id) {
        rubricService.deleteRubric(id);
        return Result.success("Rubric deleted", null);
    }
}