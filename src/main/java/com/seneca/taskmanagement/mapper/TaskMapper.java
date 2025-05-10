package com.seneca.taskmanagement.mapper;

import com.seneca.taskmanagement.domain.Bug;
import com.seneca.taskmanagement.domain.Feature;
import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.*;

import com.seneca.taskmanagement.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TaskMapper {

    @Autowired
    protected UserRepository userRepository;

    public abstract BugDto toBugDto(Bug bug);

    public abstract FeatureDto toFeatureDto(Feature feature);

    public abstract Bug toBugEntity(BugDto bugDto);

    public abstract Feature toFeatureEntity(FeatureDto featureDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    public abstract Bug toBugEntity(CreateBugDto createBugDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    public abstract Feature toFeatureEntity(CreateFeatureDto createFeatureDto);

    @Mapping(target = "createdAt", ignore = true)
    public abstract void updateBugFromDto(BugDto bugDto, @MappingTarget Bug bug);

    @Mapping(target = "createdAt", ignore = true)
    public abstract void updateFeatureFromDto(FeatureDto featureDto, @MappingTarget Feature feature);

    // Convert task to appropriate DTO based on its type
    public TaskDto toDtoByType(Task task) {
        if (task instanceof Bug) {
            return toBugDto((Bug) task);
        } else if (task instanceof Feature) {
            return toFeatureDto((Feature) task);
        }
        return null;
    }

    // Convert list of tasks to appropriate DTOs based on their types
    public List<TaskDto> toDtoListByType(List<Task> tasks) {
        return tasks.stream()
                .map(this::toDtoByType)
                .toList();
    }
}
