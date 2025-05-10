package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.*;
import com.seneca.taskmanagement.dto.*;
import com.seneca.taskmanagement.dto.UpdateBugRequest;
import com.seneca.taskmanagement.dto.UpdateFeatureRequest;
import com.seneca.taskmanagement.exception.BadRequestException;
import com.seneca.taskmanagement.exception.ResourceNotFoundException;
import com.seneca.taskmanagement.mapper.TaskMapper;
import com.seneca.taskmanagement.mapper.TaskMapperImpl;
import com.seneca.taskmanagement.repository.TaskRepository;
import com.seneca.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static com.seneca.taskmanagement.domain.Bug.BugPriority;
import static com.seneca.taskmanagement.domain.Bug.BugSeverity;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskMapper taskMapper;
    private TaskService taskService;

    private CreateBugDto createBugDto;
    private CreateFeatureDto createFeatureDto;
    private BugDto bugDto;
    private FeatureDto featureDto;
    private Bug bug;
    private Feature feature;
    private UUID userId;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapperImpl();
        taskService = new TaskService(taskRepository, userRepository, taskMapper);

        userId = UUID.randomUUID();
        UUID bugId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        createBugDto = CreateBugDto.builder()
                .name("Test Bug")
                .description("Test Bug Description")
                .severity(Bug.BugSeverity.HIGH)
                .priority(Bug.BugPriority.HIGH)
                .stepsToReproduce("1. Step one\n2. Step two")
                .environment("Production")
                .assignedUserId(userId)
                .build();

        createFeatureDto = CreateFeatureDto.builder()
                .name("Test Feature")
                .description("Test Feature Description")
                .businessValue("High business impact")
                .deadline(LocalDate.now().plusDays(30))
                .acceptanceCriteria("1. Feature works\n2. Tests pass")
                .estimatedEffort(5)
                .assignedUserId(userId)
                .build();

        bugDto = BugDto.builder()
                .id(bugId)
                .name("Test Bug")
                .description("Test Bug Description")
                .severity(Bug.BugSeverity.HIGH)
                .priority(Bug.BugPriority.HIGH)
                .stepsToReproduce("1. Step one\n2. Step two")
                .environment("Production")
                .assignedUserId(userId)
                .status(TaskStatus.OPEN)
                .build();

        featureDto = FeatureDto.builder()
                .id(featureId)
                .name("Test Feature")
                .description("Test Feature Description")
                .businessValue("High business impact")
                .deadline(LocalDate.now().plusDays(30))
                .acceptanceCriteria("1. Feature works\n2. Tests pass")
                .estimatedEffort(5)
                .assignedUserId(userId)
                .status(TaskStatus.OPEN)
                .build();

        bug = Bug.builder()
                .id(bugId)
                .name("Test Bug")
                .description("Test Bug Description")
                .severity(Bug.BugSeverity.HIGH)
                .status(TaskStatus.OPEN)
                .build();

        feature = Feature.builder()
                .id(featureId)
                .name("Test Feature")
                .description("Test Feature Description")
                .businessValue("Critical business value")
                .deadline(LocalDate.now().plusDays(30))
                .acceptanceCriteria("1. Feature works\n2. Tests pass")
                .estimatedEffort(5)
                .status(TaskStatus.OPEN)
                .build();
    }

    @Test
    void createTask_Bug_Success() {
        // Arrange
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        when(taskRepository.save(any(Bug.class))).thenReturn(bug);

        // Act
        TaskDto result = taskService.createTask(createBugDto);

        // Assert
        assertInstanceOf(BugDto.class, result);
        BugDto resultBug = (BugDto) result;
        assertEquals(bugDto.getName(), resultBug.getName());
        assertEquals(bugDto.getSeverity(), resultBug.getSeverity());
        assertEquals(TaskStatus.OPEN, resultBug.getStatus());

        verify(userRepository).existsById(userId);
        verify(taskRepository).save(any(Bug.class));
    }

    @Test
    void createTask_Feature_Success() {
        // Arrange
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        when(taskRepository.save(any(Feature.class))).thenReturn(feature);

        // Act
        TaskDto result = taskService.createTask(createFeatureDto);

        // Assert
        assertInstanceOf(FeatureDto.class, result);
        FeatureDto resultFeature = (FeatureDto) result;
        assertEquals(featureDto.getName(), resultFeature.getName());
        assertEquals(featureDto.getDeadline(), resultFeature.getDeadline());
        assertEquals(featureDto.getEstimatedEffort(), resultFeature.getEstimatedEffort());
        assertEquals(TaskStatus.OPEN, resultFeature.getStatus());

        verify(userRepository).existsById(userId);
        verify(taskRepository).save(any(Feature.class));
    }

    @Test
    void createTask_InvalidUser_ThrowsException() {
        // Arrange
        when(userRepository.existsById(any(UUID.class))).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(createBugDto));
        verify(userRepository).existsById(userId);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void getTaskById_Bug_Success() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(bug));

        // Act
        TaskDto result = taskService.getTaskById(bug.getId());

        // Assert
        assertInstanceOf(BugDto.class, result);
        assertEquals(bug.getId(), result.getId());
        assertEquals(bug.getName(), result.getName());
        verify(taskRepository).findById(bug.getId());
    }

    @Test
    void getTaskById_NotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(UUID.randomUUID()));
        verify(taskRepository).findById(any(UUID.class));
    }

    @Test
    void getAllTasks_Success() {
        // Arrange
        List<Task> tasks = Arrays.asList(bug, feature);
        when(taskRepository.findAll()).thenReturn(tasks);

        // Act
        List<TaskDto> results = taskService.getAllTasks();

        // Assert
        assertEquals(2, results.size());
        assertInstanceOf(BugDto.class, results.get(0));
        assertInstanceOf(FeatureDto.class, results.get(1));
        verify(taskRepository).findAll();
    }

    @Test
    void findTasksWithFilters_Success() {
        // Arrange
        List<Task> tasks = Arrays.asList(bug, feature);
        Page<Task> taskPage = new PageImpl<>(tasks);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(any())).thenReturn(true);
        when(taskRepository.findTasksWithFilters(any(), any(), any(), any())).thenReturn(taskPage);

        // Act
        Page<TaskDto> result = taskService.findTasksWithFilters(
            Optional.of(userId),
            Optional.of(TaskStatus.OPEN),
            Optional.of("test"),
            pageable
        );

        // Assert
        assertEquals(2, result.getContent().size());
        assertInstanceOf(BugDto.class, result.getContent().get(0));
        assertInstanceOf(FeatureDto.class, result.getContent().get(1));
        
        verify(userRepository).existsById(userId);
        verify(taskRepository).findTasksWithFilters(any(), any(), any(), any());
    }

    @Test
    void updateBugTask_Success() {
        // Given
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Bug existingBug = new Bug();
        existingBug.setId(taskId);
        existingBug.setName("Old bug name");
        existingBug.setPriority(BugPriority.LOW);
        existingBug.setSeverity(BugSeverity.LOW);

        User user = new User();
        user.setId(userId);

        UpdateBugRequest updateRequest = new UpdateBugRequest();
        updateRequest.setName("Updated bug name");
        updateRequest.setPriority(Bug.BugPriority.HIGH);
        updateRequest.setSeverity(Bug.BugSeverity.HIGH);
        updateRequest.setAssignedUserId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingBug));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Bug.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        TaskDto result = taskService.updateTask(taskId, updateRequest);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(taskRepository).save(argThat(task -> {
            Bug bug = (Bug) task;
            return bug.getName().equals("Updated bug name") &&
                   bug.getPriority() == Bug.BugPriority.HIGH &&
                   bug.getSeverity() == Bug.BugSeverity.HIGH &&
                   bug.getAssignedUser().equals(user);
        }));
    }

    @Test
    void updateFeatureTask_Success() {
        // Given
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Feature existingFeature = new Feature();
        existingFeature.setId(taskId);
        existingFeature.setName("Old feature name");
        existingFeature.setBusinessValue("Old value");

        User user = new User();
        user.setId(userId);

        UpdateFeatureRequest updateRequest = new UpdateFeatureRequest();
        updateRequest.setName("Updated feature name");
        updateRequest.setBusinessValue("Updated value");
        updateRequest.setEstimatedEffort(5);
        updateRequest.setAssignedUserId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingFeature));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Feature.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        TaskDto result = taskService.updateTask(taskId, updateRequest);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(taskRepository).save(argThat(task -> {
            Feature feature = (Feature) task;
            return feature.getName().equals("Updated feature name") &&
                   feature.getBusinessValue().equals("Updated value") &&
                   feature.getEstimatedEffort() == 5 &&
                   feature.getAssignedUser().equals(user);
        }));
    }

    @Test
    void updateTask_TypeMismatch_ThrowsBadRequestException() {
        // Given
        UUID taskId = UUID.randomUUID();
        Bug existingBug = new Bug();
        existingBug.setId(taskId);

        UpdateFeatureRequest updateRequest = new UpdateFeatureRequest();
        updateRequest.setName("Updated name");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingBug));

        // When/Then
        assertThrows(BadRequestException.class, () -> 
            taskService.updateTask(taskId, updateRequest));
    }

    @Test
    void updateTask_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Bug existingBug = new Bug();
        existingBug.setId(taskId);

        UpdateBugRequest updateRequest = new UpdateBugRequest();
        updateRequest.setAssignedUserId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingBug));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> 
            taskService.updateTask(taskId, updateRequest));
    }

    @Test
    void updateTask_PartialUpdate_Success() {
        // Given
        UUID taskId = UUID.randomUUID();
        Bug existingBug = new Bug();
        existingBug.setId(taskId);
        existingBug.setName("Old name");
        existingBug.setDescription("Old description");
        existingBug.setPriority(BugPriority.LOW);
        existingBug.setSeverity(BugSeverity.LOW);

        UpdateBugRequest updateRequest = new UpdateBugRequest();
        updateRequest.setPriority(BugPriority.HIGH); // Only update priority

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingBug));
        when(taskRepository.save(any(Bug.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        TaskDto result = taskService.updateTask(taskId, updateRequest);

        // Then
        assertNotNull(result);
        verify(taskRepository).save(argThat(task -> {
            Bug bug = (Bug) task;
            return bug.getName().equals("Old name") && // Should remain unchanged
                   bug.getDescription().equals("Old description") && // Should remain unchanged
                   bug.getPriority() == BugPriority.HIGH && // Should be updated
                   bug.getSeverity() == BugSeverity.LOW; // Should remain unchanged
        }));
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(bug));
        when(taskRepository.save(any(Bug.class))).thenReturn(bug);

        // Act
        taskService.deleteTask(bug.getId());

        // Assert
        verify(taskRepository).findById(bug.getId());
        verify(taskRepository).save(any(Bug.class));
    }

    @Test
    void deleteTask_NotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(UUID.randomUUID()));
        verify(taskRepository).findById(any(UUID.class));
        verifyNoMoreInteractions(taskRepository);
    }
}
