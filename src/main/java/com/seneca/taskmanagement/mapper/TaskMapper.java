package com.seneca.taskmanagement.mapper;

import com.seneca.taskmanagement.domain.Bug;
import com.seneca.taskmanagement.domain.Feature;
import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.*;
import com.seneca.taskmanagement.exception.ResourceNotFoundException;
import com.seneca.taskmanagement.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TaskMapper {

    @Autowired
    protected UserRepository userRepository;
    
    // Add setter for testing
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    public abstract BugDto toBugDto(Bug bug);

    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    public abstract FeatureDto toFeatureDto(Feature feature);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
    public abstract Bug toBugEntity(BugDto bugDto);

    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
    public abstract Feature toFeatureEntity(FeatureDto featureDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
    @Mapping(target = "status", source = "status")
    public abstract Bug toBugEntity(CreateBugDto createBugDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
    public abstract Feature toFeatureEntity(CreateFeatureDto createFeatureDto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
    public abstract void updateBugFromDto(BugDto bugDto, @MappingTarget Bug bug);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "assignedUser", source = "assignedUserId", qualifiedByName = "mapAssignedUser")
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
    
    /**
     * Maps a user ID to a User entity for task mapping
     */
    @Named("mapAssignedUser")
    protected User mapAssignedUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
