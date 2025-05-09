package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.*;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<TaskDto> findTasksWithFilters(
            Optional<UUID> userId,
            Optional<TaskStatus> status,
            Optional<String> searchTerm,
            Pageable pageable) {

        // Validate user ID if provided
        if (userId.isPresent()) {
            validateUserExists(userId.get());
        }

        Page<Task> taskPage = taskRepository.findTasksWithFilters(userId, status, searchTerm, pageable);
        return taskPage.map(taskMapper::toDtoByType);
    }

    /**
     * Update a task
     *
     * @param id      task ID
     * @param taskDto updated task data
     * @return updated task
     * @throws ResourceNotFoundException if task not found
     * @throws BadRequestException if task type mismatch
     */
    @Transactional
    public TaskDto updateTask(UUID id, TaskDto taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        validateUserExists(taskDto.getAssignedUserId());

        // Ensure the task type matches
        if (taskDto instanceof BugDto && !(task instanceof Bug)) {
            throw new BadRequestException("Task with ID: " + id + " is not a bug");
        } else if (taskDto instanceof FeatureDto && !(task instanceof Feature)) {
            throw new BadRequestException("Task with ID: " + id + " is not a feature");
        }

        Task updatedTask;
        if (taskDto instanceof BugDto && task instanceof Bug) {
            taskMapper.updateBugFromDto((BugDto) taskDto, (Bug) task);
            updatedTask = taskRepository.save(task);
        } else if (taskDto instanceof FeatureDto && task instanceof Feature) {
            taskMapper.updateFeatureFromDto((FeatureDto) taskDto, (Feature) task);
            updatedTask = taskRepository.save(task);
        } else {
            throw new BadRequestException("Invalid task type. Must be either BUG or FEATURE");
        }

        log.info("Updated {} task with ID: {}", updatedTask.getClass().getSimpleName(), updatedTask.getId());
        return taskMapper.toDtoByType(updatedTask);
    }

    /**
     * Delete a task
     *
     * @param id task ID
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional
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
