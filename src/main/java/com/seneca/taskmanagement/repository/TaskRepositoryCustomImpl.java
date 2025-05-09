package com.seneca.taskmanagement.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.seneca.taskmanagement.domain.QTask;
import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryCustomImpl implements TaskRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Task> findTasksWithFilters(
            Optional<UUID> userId,
            Optional<TaskStatus> status,
            Optional<String> searchTerm,
            Pageable pageable) {

        QTask task = QTask.task;
        BooleanBuilder predicate = new BooleanBuilder();

        // Apply filters if present
        userId.ifPresent(id -> predicate.and(task.assignedUser.id.eq(id)));
        status.ifPresent(s -> predicate.and(task.status.eq(s)));
        searchTerm.ifPresent(term -> {
            String pattern = "%" + term.toLowerCase() + "%";
            predicate.and(task.name.toLowerCase().like(pattern));
        });

        // Create the query
        JPAQuery<Task> query = new JPAQuery<>(entityManager);
        query.select(task).from(task).where(predicate);

        // Count total results
        long total = query.fetchCount();

        // Apply pagination
        List<Task> tasks = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(task.createdAt.desc())
                .fetch();

        return new PageImpl<>(tasks, pageable, total);
    }
}
