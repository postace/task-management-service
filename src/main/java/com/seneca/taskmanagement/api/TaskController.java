package com.seneca.taskmanagement.api;

import com.seneca.taskmanagement.domain.TaskStatus;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.PaginatedResponse;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.dto.UpdateTaskRequest;
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
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task (bug or feature) with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Assigned user not found")
    })
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
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
    @Operation(summary = "Get all tasks", description = "Returns a paginated list of all tasks with optional filtering")
    @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
    public ResponseEntity<PaginatedResponse<TaskDto>> getTasks(
            @Parameter(description = "Filter tasks by user ID") @RequestParam(required = false) Optional<UUID> userId,
            @Parameter(description = "Filter tasks by status") @RequestParam(required = false) Optional<TaskStatus> status,
            @Parameter(description = "Search tasks by name") @RequestParam(required = false) Optional<String> searchTerm,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<TaskDto> tasks = taskService.findTasksWithFilters(userId, status, searchTerm, pageable);
        return ResponseEntity.ok(PaginatedResponse.from(tasks));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates a task (bug or feature) with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDto> updateTask(
            @Parameter(description = "ID of the task to update") @PathVariable UUID id,
            @RequestBody UpdateTaskRequest updateRequest) {
        TaskDto updatedTask = taskService.updateTask(id, updateRequest);
        return ResponseEntity.ok(updatedTask);
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
