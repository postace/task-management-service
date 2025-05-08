package com.seneca.taskmanagement.mapper;

import com.seneca.taskmanagement.domain.Bug;
import com.seneca.taskmanagement.domain.Feature;
import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TaskMapper {

    @Autowired
    protected UserRepository userRepository;

//    @Mapping(target = "assignedUserId", source = "assignedUser.id")
//    public abstract TaskDto toDto(Task task);

    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    public abstract BugDto toBugDto(Bug bug);

    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    public abstract FeatureDto toFeatureDto(Feature feature);

//    public abstract List<TaskDto> toDtoList(List<Task> tasks);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapUserFromId")
    public abstract Bug toBugEntity(BugDto bugDto);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapUserFromId")
    public abstract Feature toFeatureEntity(FeatureDto featureDto);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapUserFromId")
    public abstract void updateBugFromDto(BugDto bugDto, @MappingTarget Bug bug);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapUserFromId")
    public abstract void updateFeatureFromDto(FeatureDto featureDto, @MappingTarget Feature feature);

    @Named("mapUserFromId")
    protected User mapUserFromId(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

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
