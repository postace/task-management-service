package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.*;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.dto.UpdateBugRequest;
import com.seneca.taskmanagement.dto.UpdateFeatureRequest;
import com.seneca.taskmanagement.dto.UpdateTaskRequest;
import com.seneca.taskmanagement.exception.BadRequestException;
import com.seneca.taskmanagement.exception.ResourceNotFoundException;
import com.seneca.taskmanagement.mapper.TaskMapper;
import com.seneca.taskmanagement.repository.TaskRepository;
import com.seneca.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    /**
     * Create a new task
     *
     * @param taskDto task data (can be bug or feature)
     * @return created task
     * @throws ResourceNotFoundException if assigned user not found
     * @throws BadRequestException if task type is invalid
     */
    public TaskDto createTask(TaskDto taskDto) {
        validateUserExists(taskDto.getAssignedUserId());
        Task task;
        if (taskDto instanceof BugDto) {
            task = taskMapper.toBugEntity((BugDto) taskDto);
        } else if (taskDto instanceof FeatureDto) {
            task = taskMapper.toFeatureEntity((FeatureDto) taskDto);
        } else {
            throw new BadRequestException("Invalid task type. Must be either BUG or FEATURE");
        }
        Task savedTask = taskRepository.save(task);
        log.info("Created {} task with ID: {}", savedTask.getClass().getSimpleName(), savedTask.getId());
        return taskMapper.toDtoByType(savedTask);
    }

    /**
     * Get a task by ID
     *
     * @param id task ID
     * @return task data
     * @throws ResourceNotFoundException if task not found
     */
    public TaskDto getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        return taskMapper.toDtoByType(task);
    }

    /**
     * Get all tasks
     *
     * @return list of all tasks
     */
    public List<TaskDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.toDtoListByType(tasks);
    }

    /**
     * Find tasks with filters
     *
     * @param userId     optional user ID filter
     * @param status     optional status filter
     * @param searchTerm optional search term for task name
     * @param pageable   pagination information
     * @return page of filtered tasks
     */
    public Page<TaskDto> findTasksWithFilters(
            Optional<UUID> userId,
            Optional<TaskStatus> status,
            Optional<String> searchTerm,
            Pageable pageable) {

        // Validate user ID if provided
        userId.ifPresent(this::validateUserExists);

        Page<Task> taskPage = taskRepository.findTasksWithFilters(userId, status, searchTerm, pageable);
        return taskPage.map(taskMapper::toDtoByType);
    }

    /**
     * Update a task
     *
     * @param id           task ID
     * @param updateRequest updated task data
     * @return updated task
     * @throws ResourceNotFoundException if task not found
     * @throws BadRequestException if task type mismatch
     */
    public TaskDto updateTask(UUID id, UpdateTaskRequest updateRequest) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Verify task type matches
        if (task instanceof Bug && !(updateRequest instanceof UpdateBugRequest) ||
            task instanceof Feature && !(updateRequest instanceof UpdateFeatureRequest)) {
            throw new BadRequestException("Task type mismatch. Cannot update " + 
                task.getClass().getSimpleName() + " with " + updateRequest.getClass().getSimpleName());
        }

        // Update common fields if they are not null
        if (updateRequest.getName() != null) {
            task.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            task.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getStatus() != null) {
            task.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getAssignedUserId() != null) {
            User user = userRepository.findById(updateRequest.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + updateRequest.getAssignedUserId()));
            task.setAssignedUser(user);
        }

        // Update type-specific fields
        if (task instanceof Bug && updateRequest instanceof UpdateBugRequest) {
            updateBugFields((Bug) task, (UpdateBugRequest) updateRequest);
        } else if (task instanceof Feature && updateRequest instanceof UpdateFeatureRequest) {
            updateFeatureFields((Feature) task, (UpdateFeatureRequest) updateRequest);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toDtoByType(updatedTask);
    }

    private void updateBugFields(Bug bug, UpdateBugRequest updateRequest) {
        if (updateRequest.getSeverity() != null) {
            bug.setSeverity(updateRequest.getSeverity());
        }
        if (updateRequest.getPriority() != null) {
            bug.setPriority(updateRequest.getPriority());
        }
        if (updateRequest.getStepsToReproduce() != null) {
            bug.setStepsToReproduce(updateRequest.getStepsToReproduce());
        }
        if (updateRequest.getEnvironment() != null) {
            bug.setEnvironment(updateRequest.getEnvironment());
        }
    }

    private void updateFeatureFields(Feature feature, UpdateFeatureRequest updateRequest) {
        if (updateRequest.getBusinessValue() != null) {
            feature.setBusinessValue(updateRequest.getBusinessValue());
        }
        if (updateRequest.getDeadline() != null) {
            feature.setDeadline(updateRequest.getDeadline());
        }
        if (updateRequest.getAcceptanceCriteria() != null) {
            feature.setAcceptanceCriteria(updateRequest.getAcceptanceCriteria());
        }
        if (updateRequest.getEstimatedEffort() != null) {
            feature.setEstimatedEffort(updateRequest.getEstimatedEffort());
        }
    }

    /**
     * Delete a task
     *
     * @param id task ID
     * @throws ResourceNotFoundException if task not found
     */
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        task.setDeleted(true);
        task.setDeletedAt(java.time.LocalDateTime.now());
        taskRepository.save(task);
        log.info("Soft deleted task with ID: {}", id);
    }

    /**
     * Validate that a user exists
     *
     * @param userId user ID to validate
     * @throws ResourceNotFoundException if user not found
     */
    private void validateUserExists(UUID userId) {
        if (userId != null && !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }
}
