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

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    /**
     * Create a new bug task
     *
     * @param bugDto bug task data
     * @return created bug task
     * @throws ResourceNotFoundException if assigned user not found
     */
    @Transactional
    public TaskDto createBug(BugDto bugDto) {
        validateUserExists(bugDto.getAssignedUserId());
        Bug bug = taskMapper.toBugEntity(bugDto);
        Bug savedBug = taskRepository.save(bug);
        log.info("Created bug task with ID: {}", savedBug.getId());
        return taskMapper.toBugDto(savedBug);
    }

    /**
     * Create a new feature task
     *
     * @param featureDto feature task data
     * @return created feature task
     * @throws ResourceNotFoundException if assigned user not found
     */
    @Transactional
    public TaskDto createFeature(FeatureDto featureDto) {
        validateUserExists(featureDto.getAssignedUserId());
        Feature feature = taskMapper.toFeatureEntity(featureDto);
        Feature savedFeature = taskRepository.save(feature);
        log.info("Created feature task with ID: {}", savedFeature.getId());
        return taskMapper.toFeatureDto(savedFeature);
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
     * Update a bug task
     *
     * @param id     bug task ID
     * @param bugDto updated bug task data
     * @return updated bug task
     * @throws ResourceNotFoundException if task not found or not a bug
     */
    @Transactional
    public TaskDto updateBug(UUID id, BugDto bugDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        if (!(task instanceof Bug)) {
            throw new BadRequestException("Task with ID: " + id + " is not a bug");
        }

        validateUserExists(bugDto.getAssignedUserId());

        Bug bug = (Bug) task;
        taskMapper.updateBugFromDto(bugDto, bug);
        Bug updatedBug = taskRepository.save(bug);
        log.info("Updated bug task with ID: {}", updatedBug.getId());
        return taskMapper.toBugDto(updatedBug);
    }

    /**
     * Update a feature task
     *
     * @param id         feature task ID
     * @param featureDto updated feature task data
     * @return updated feature task
     * @throws ResourceNotFoundException if task not found or not a feature
     */
    @Transactional
    public TaskDto updateFeature(UUID id, FeatureDto featureDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        if (!(task instanceof Feature)) {
            throw new BadRequestException("Task with ID: " + id + " is not a feature");
        }

        validateUserExists(featureDto.getAssignedUserId());

        Feature feature = (Feature) task;
        taskMapper.updateFeatureFromDto(featureDto, feature);
        Feature updatedFeature = taskRepository.save(feature);
        log.info("Updated feature task with ID: {}", updatedFeature.getId());
        return taskMapper.toFeatureDto(updatedFeature);
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
