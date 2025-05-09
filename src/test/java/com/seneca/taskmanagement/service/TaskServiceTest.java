package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.*;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
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

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskMapper taskMapper;
    private TaskService taskService;

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

        bugDto = BugDto.builder()
                .id(bugId)
                .name("Test Bug")
                .description("Test Bug Description")
                .severity(Bug.BugSeverity.HIGH)
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
        TaskDto result = taskService.createTask(bugDto);

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
        TaskDto result = taskService.createTask(featureDto);

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
        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(bugDto));
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
    void updateTask_Bug_Success() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(bug));
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        when(taskRepository.save(any(Bug.class))).thenReturn(bug);

        BugDto updateDto = BugDto.builder()
                .name("Updated Bug")
                .description("Updated Description")
                .severity(Bug.BugSeverity.LOW)
                .assignedUserId(userId)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // Act
        TaskDto result = taskService.updateTask(bug.getId(), updateDto);

        // Assert
        assertInstanceOf(BugDto.class, result);
        BugDto resultBug = (BugDto) result;
        assertEquals(updateDto.getName(), resultBug.getName());
        assertEquals(updateDto.getSeverity(), resultBug.getSeverity());
        assertEquals(updateDto.getStatus(), resultBug.getStatus());

        verify(userRepository).existsById(userId);
        verify(taskRepository).save(any(Bug.class));
    }

    @Test
    void updateTask_TypeMismatch_ThrowsException() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(bug));
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> taskService.updateTask(bug.getId(), featureDto));
        verify(taskRepository).findById(bug.getId());
        verify(userRepository).existsById(userId);
        verifyNoMoreInteractions(taskRepository);
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
