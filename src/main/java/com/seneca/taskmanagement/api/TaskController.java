package com.seneca.taskmanagement.api;

import com.seneca.taskmanagement.domain.TaskStatus;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/bugs")
    @Operation(summary = "Create a new bug", description = "Creates a new bug task with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bug created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Assigned user not found")
    })
    public ResponseEntity<TaskDto> createBug(@Valid @RequestBody BugDto bugDto) {
        TaskDto createdBug = taskService.createBug(bugDto);
        return new ResponseEntity<>(createdBug, HttpStatus.CREATED);
    }

    @PostMapping("/features")
    @Operation(summary = "Create a new feature", description = "Creates a new feature task with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Feature created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Assigned user not found")
    })
    public ResponseEntity<TaskDto> createFeature(@Valid @RequestBody FeatureDto featureDto) {
        TaskDto createdFeature = taskService.createFeature(featureDto);
        return new ResponseEntity<>(createdFeature, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Returns a task based on the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDto> getTaskById(
            @Parameter(description = "ID of the task to retrieve") @PathVariable UUID id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks with optional filtering")
    @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
    public ResponseEntity<Page<TaskDto>> getTasks(
            @Parameter(description = "Filter tasks by user ID") @RequestParam(required = false) Optional<UUID> userId,
            @Parameter(description = "Filter tasks by status") @RequestParam(required = false) Optional<TaskStatus> status,
            @Parameter(description = "Search tasks by name") @RequestParam(required = false) Optional<String> searchTerm,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<TaskDto> tasks = taskService.findTasksWithFilters(userId, status, searchTerm, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/bugs/{id}")
    @Operation(summary = "Update a bug", description = "Updates a bug task with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bug updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or task is not a bug"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDto> updateBug(
            @Parameter(description = "ID of the bug to update") @PathVariable UUID id,
            @Valid @RequestBody BugDto bugDto) {
        TaskDto updatedBug = taskService.updateBug(id, bugDto);
        return ResponseEntity.ok(updatedBug);
    }

    @PutMapping("/features/{id}")
    @Operation(summary = "Update a feature", description = "Updates a feature task with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feature updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or task is not a feature"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDto> updateFeature(
            @Parameter(description = "ID of the feature to update") @PathVariable UUID id,
            @Valid @RequestBody FeatureDto featureDto) {
        TaskDto updatedFeature = taskService.updateFeature(id, featureDto);
        return ResponseEntity.ok(updatedFeature);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task based on the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the task to delete") @PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
