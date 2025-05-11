package com.seneca.taskmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seneca.taskmanagement.config.TestContainersConfig;
import com.seneca.taskmanagement.domain.Bug;
import com.seneca.taskmanagement.domain.Bug.BugPriority;
import com.seneca.taskmanagement.domain.Bug.BugSeverity;
import com.seneca.taskmanagement.domain.Feature;
import com.seneca.taskmanagement.domain.Task;
import com.seneca.taskmanagement.domain.TaskStatus;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.dto.UserDto;
import com.seneca.taskmanagement.repository.TaskRepository;
import com.seneca.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class TaskControllerIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

    private UserDto createTestUser(String username) throws Exception {
        UserDto userDto = UserDto.builder()
                .username(username)
                .fullName("Test User")
                .build();

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    @Test
    void bugTaskCrudOperations() throws Exception {
        // First create a user to assign the task to
        UserDto user = createTestUser("testuser1");

        // Create a bug task
        BugDto bugDto = BugDto.builder()
                .name("Critical Login Bug")
                .description("Users unable to login")
                .assignedUserId(user.getId())
                .severity(BugSeverity.HIGH)
                .priority(BugPriority.HIGH)
                .status(TaskStatus.OPEN)
                .build();

        // Test Create Bug Task
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bugDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Critical Login Bug"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.taskType").value("BUG"))
                .andReturn();

        // Extract created task ID
        TaskDto createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDto.class);
        UUID taskId = createdTask.getId();
        assertNotNull(taskId);
        
        // Verify task was saved in the database
        Optional<Task> savedTaskOpt = taskRepository.findById(taskId);
        assertTrue(savedTaskOpt.isPresent(), "Task should exist in database");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Bug.class, savedTask, "Task should be a Bug");
        Bug savedBug = (Bug) savedTask;
        assertEquals("Critical Login Bug", savedBug.getName());
        assertEquals("Users unable to login", savedBug.getDescription());
        assertEquals(BugSeverity.HIGH, savedBug.getSeverity());
        assertEquals(BugPriority.HIGH, savedBug.getPriority());
        assertEquals(TaskStatus.OPEN, savedBug.getStatus());
//        assertEquals(user.getId(), savedBug.getAssignedUser().getId());

        // Test Get Task by ID
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.name").value("Critical Login Bug"))
                .andExpect(jsonPath("$.severity").value("HIGH"));

        // Test Update Bug Task
        BugDto updateDto = BugDto.builder()
                .id(taskId)
                .name("Critical Login Bug")
                .description("Users unable to login with OAuth")
                .assignedUserId(user.getId())
                .severity(BugSeverity.CRITICAL)
                .priority(BugPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));
                
        // Verify database was updated with new values
        savedTaskOpt = taskRepository.findById(taskId);
        assertTrue(savedTaskOpt.isPresent(), "Task should still exist after update");
        savedTask = savedTaskOpt.get();
        assertInstanceOf(Bug.class, savedTask, "Task should still be a Bug after update");
        savedBug = (Bug) savedTask;
        assertEquals("Critical Login Bug", savedBug.getName());
        assertEquals("Users unable to login with OAuth", savedBug.getDescription());
        assertEquals(BugSeverity.CRITICAL, savedBug.getSeverity());
        assertEquals(TaskStatus.IN_PROGRESS, savedBug.getStatus());

        // Test Get All Tasks
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.hasPrevious").exists());

        // Test Delete Task
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
                
        // Verify task is soft-deleted in database (still exists but marked as deleted)
        savedTaskOpt = taskRepository.findById(taskId);
        assertFalse(savedTaskOpt.isPresent(), "Task should appear deleted through repository");
    }

    @Test
    void featureTaskCrudOperations() throws Exception {
        // First create a user to assign the task to
        UserDto user = createTestUser("testuser2");

        // Create a feature task
        FeatureDto featureDto = FeatureDto.builder()
                .name("Add OAuth Support")
                .description("Implement OAuth authentication")
                .assignedUserId(user.getId())
                .deadline(LocalDate.now().plusWeeks(2))
                .businessValue("High ROI potential")
                .estimatedEffort(5)
                .status(TaskStatus.OPEN)
                .build();

        // Test Create Feature Task
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(featureDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Add OAuth Support"))
                .andExpect(jsonPath("$.taskType").value("FEATURE"))
                .andExpect(jsonPath("$.businessValue").value("High ROI potential"))
                .andExpect(jsonPath("$.estimatedEffort").value(5))
                .andReturn();

        // Extract created task ID
        TaskDto createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDto.class);
        UUID taskId = createdTask.getId();
        assertNotNull(taskId);
        
        // Verify task was saved in the database
        Optional<Task> savedTaskOpt = taskRepository.findById(taskId);
        assertTrue(savedTaskOpt.isPresent(), "Task should exist in database");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Feature.class, savedTask, "Task should be a Feature");
        Feature savedFeature = (Feature) savedTask;
        assertEquals("Add OAuth Support", savedFeature.getName());
        assertEquals("Implement OAuth authentication", savedFeature.getDescription());
        assertEquals("High ROI potential", savedFeature.getBusinessValue());
        assertEquals(Integer.valueOf(5), savedFeature.getEstimatedEffort());
        assertEquals(TaskStatus.OPEN, savedFeature.getStatus());
//        assertEquals(user.getId(), savedFeature.getAssignedUser().getId());
        assertNotNull(savedFeature.getDeadline(), "Deadline should be set");

        // Test Get Task by ID
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.name").value("Add OAuth Support"))
                .andExpect(jsonPath("$.businessValue").value("High ROI potential"))
                .andExpect(jsonPath("$.estimatedEffort").value(5));

        // Test Update Feature Task
        FeatureDto updateDto = FeatureDto.builder()
                .id(taskId)
                .name("Add OAuth Support")
                .description("Implement OAuth authentication with Google")
                .assignedUserId(user.getId())
                .deadline(LocalDate.now().plusWeeks(3))
                .businessValue("Very high business impact")
                .estimatedEffort(8)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.businessValue").value("Very high business impact"))
                .andExpect(jsonPath("$.estimatedEffort").value(8));
                
        // Verify database was updated with new values
        savedTaskOpt = taskRepository.findById(taskId);
        assertTrue(savedTaskOpt.isPresent(), "Task should still exist after update");
        savedTask = savedTaskOpt.get();
        assertInstanceOf(Feature.class, savedTask, "Task should still be a Feature after update");
        savedFeature = (Feature) savedTask;
        assertEquals("Add OAuth Support", savedFeature.getName());
        assertEquals("Implement OAuth authentication with Google", savedFeature.getDescription());
        assertEquals("Very high business impact", savedFeature.getBusinessValue());
        assertEquals(Integer.valueOf(8), savedFeature.getEstimatedEffort());
        assertEquals(TaskStatus.IN_PROGRESS, savedFeature.getStatus());

        // Test Get All Tasks
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.hasPrevious").exists());

        // Test Delete Task
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verify task is deleted via API
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
                
        // Verify task is soft-deleted in database (still exists but marked as deleted)
        savedTaskOpt = taskRepository.findById(taskId);
        assertFalse(savedTaskOpt.isPresent(), "Task should appear deleted through repository");
    }
    
    @Test
    void shouldFilterTasksByStatus() throws Exception {
        // Create user
        UserDto user = createTestUser("filterUser");

        // Create two tasks with different statuses
        BugDto openBugDto = BugDto.builder()
                .name("Open Bug")
                .description("This is an open bug")
                .assignedUserId(user.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.OPEN)
                .build();

        BugDto inProgressBugDto = BugDto.builder()
                .name("In Progress Bug")
                .description("This is an in-progress bug")
                .assignedUserId(user.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // Create the tasks
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openBugDto)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inProgressBugDto)))
            .andExpect(status().isCreated());

        // Filter tasks by status and verify
        mockMvc.perform(get("/tasks")
                .param("status", "OPEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("Open Bug"))
            .andExpect(jsonPath("$.items[0].status").value("OPEN"));

        mockMvc.perform(get("/tasks")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("In Progress Bug"))
            .andExpect(jsonPath("$.items[0].status").value("IN_PROGRESS"));

        // Verify database contains both tasks with correct status
        long openTasksCount = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() == TaskStatus.OPEN)
                .count();
        long inProgressTasksCount = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        assertTrue(openTasksCount >= 1, "Database should have at least one OPEN task");
        assertTrue(inProgressTasksCount >= 1, "Database should have at least one IN_PROGRESS task");
    }

    @Test
    void shouldFilterTasksByAssignedUser() throws Exception {
        // Create two users
        UserDto user1 = createTestUser("user1ForFiltering");
        UserDto user2 = createTestUser("user2ForFiltering");

        // Create tasks assigned to different users
        BugDto user1Bug = BugDto.builder()
                .name("User 1 Bug")
                .description("This is user 1's bug")
                .assignedUserId(user1.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.OPEN)
                .build();

        BugDto user2Bug = BugDto.builder()
                .name("User 2 Bug")
                .description("This is user 2's bug")
                .assignedUserId(user2.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.OPEN)
                .build();

        // Create the tasks
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Bug)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Bug)))
            .andExpect(status().isCreated());

        // Filter tasks by user and verify
        mockMvc.perform(get("/tasks")
                .param("userId", user1.getId().toString()))
            .andExpect(status().isOk())
                .andDo(print())
            .andExpect(jsonPath("$.items[0].name").value("User 1 Bug"))
            .andExpect(jsonPath("$.items[0].assignedUserId").value(user1.getId().toString()));

        mockMvc.perform(get("/tasks")
                .param("userId", user2.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("User 2 Bug"))
            .andExpect(jsonPath("$.items[0].assignedUserId").value(user2.getId().toString()));

        // Verify database relationships
        long user1TaskCount = taskRepository.findAll().stream()
                .filter(task -> task.getAssignedUser() != null &&
                       task.getAssignedUser().getId().equals(user1.getId()))
                .count();
        long user2TaskCount = taskRepository.findAll().stream()
                .filter(task -> task.getAssignedUser() != null &&
                       task.getAssignedUser().getId().equals(user2.getId()))
                .count();

        assertTrue(user1TaskCount >= 1, "Database should have at least one task assigned to user1");
        assertTrue(user2TaskCount >= 1, "Database should have at least one task assigned to user2");
    }
}
