package com.seneca.taskmanagement.repository;

import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TaskRepositoryCustom {
    
    /**
     * Find tasks with filters applied
     * 
     * @param userId Optional user ID to filter tasks by assignee
     * @param status Optional status to filter tasks by status
     * @param searchTerm Optional search term to filter tasks by name
     * @param pageable Pagination information
     * @return Page of filtered tasks
     */
    Page<Task> findTasksWithFilters(
            Optional<Long> userId,
            Optional<TaskStatus> status,
            Optional<String> searchTerm,
            Pageable pageable
    );
}
