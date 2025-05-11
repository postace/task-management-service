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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private UserDto testUser;
    private BugDto testBugDto;
    private FeatureDto testFeatureDto;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test user with random username
        String randomUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        testUser = createTestUser(randomUsername);
        
        // Setup test bug data
        testBugDto = BugDto.builder()
                .name("Critical Login Bug")
                .description("Users unable to login")
                .assignedUserId(testUser.getId())
                .severity(BugSeverity.HIGH)
                .priority(BugPriority.HIGH)
                .status(TaskStatus.OPEN)
                .build();

        // Setup test feature data
        testFeatureDto = FeatureDto.builder()
                .name("Add OAuth Support")
                .description("Implement OAuth authentication")
                .assignedUserId(testUser.getId())
                .deadline(LocalDate.now().plusWeeks(2))
                .businessValue("High ROI potential")
                .estimatedEffort(5)
                .status(TaskStatus.OPEN)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clean up tasks first due to foreign key constraints
        taskRepository.deleteAll();
        // Then clean up users
        userRepository.deleteAll();
    }

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

    private TaskDto createBugTask(BugDto bugDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bugDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), TaskDto.class);
    }

    private TaskDto createFeatureTask(FeatureDto featureDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), TaskDto.class);
    }

    @Test
    void shouldCreateBugTask() throws Exception {
        // When
        MvcResult createResult = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBugDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Critical Login Bug"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.taskType").value("BUG"))
                .andReturn();

        // Then
        TaskDto createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDto.class);
        
        // Verify database state
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertTrue(savedTaskOpt.isPresent(), "Task should exist in database");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Bug.class, savedTask, "Task should be a Bug");
        Bug savedBug = (Bug) savedTask;
        assertEquals("Critical Login Bug", savedBug.getName());
        assertEquals("Users unable to login", savedBug.getDescription());
        assertEquals(BugSeverity.HIGH, savedBug.getSeverity());
        assertEquals(BugPriority.HIGH, savedBug.getPriority());
        assertEquals(TaskStatus.OPEN, savedBug.getStatus());
        assertEquals(testUser.getId(), savedBug.getAssignedUser().getId());
    }

    @Test
    void shouldRetrieveBugTask() throws Exception {
        // Given
        TaskDto createdTask = createBugTask(testBugDto);

        // When & Then
        mockMvc.perform(get("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
                .andExpect(jsonPath("$.name").value("Critical Login Bug"))
                .andExpect(jsonPath("$.severity").value("HIGH"));
    }

    @Test
    void shouldUpdateBugTask() throws Exception {
        // Given
        TaskDto createdTask = createBugTask(testBugDto);
        
        BugDto updateDto = BugDto.builder()
                .id(createdTask.getId())
                .name("Critical Login Bug")
                .description("Users unable to login with OAuth")
                .assignedUserId(testUser.getId())
                .severity(BugSeverity.CRITICAL)
                .priority(BugPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // When
        mockMvc.perform(put("/tasks/{id}", createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));

        // Then
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertTrue(savedTaskOpt.isPresent(), "Task should still exist after update");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Bug.class, savedTask, "Task should still be a Bug after update");
        Bug savedBug = (Bug) savedTask;
        assertEquals("Critical Login Bug", savedBug.getName());
        assertEquals("Users unable to login with OAuth", savedBug.getDescription());
        assertEquals(BugSeverity.CRITICAL, savedBug.getSeverity());
        assertEquals(TaskStatus.IN_PROGRESS, savedBug.getStatus());
    }

    @Test
    void shouldDeleteBugTask() throws Exception {
        // Given
        TaskDto createdTask = createBugTask(testBugDto);

        // When
        mockMvc.perform(delete("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isNoContent());

        // Then
        // Verify task is deleted via API
        mockMvc.perform(get("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isNotFound());
                
        // Verify task is soft-deleted in database
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertFalse(savedTaskOpt.isPresent(), "Task should appear deleted through repository");
    }

    @Test
    void shouldCreateFeatureTask() throws Exception {
        // When
        MvcResult createResult = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFeatureDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Add OAuth Support"))
                .andExpect(jsonPath("$.taskType").value("FEATURE"))
                .andExpect(jsonPath("$.businessValue").value("High ROI potential"))
                .andExpect(jsonPath("$.estimatedEffort").value(5))
                .andReturn();

        // Then
        TaskDto createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDto.class);
        
        // Verify database state
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertTrue(savedTaskOpt.isPresent(), "Task should exist in database");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Feature.class, savedTask, "Task should be a Feature");
        Feature savedFeature = (Feature) savedTask;
        assertEquals("Add OAuth Support", savedFeature.getName());
        assertEquals("Implement OAuth authentication", savedFeature.getDescription());
        assertEquals("High ROI potential", savedFeature.getBusinessValue());
        assertEquals(Integer.valueOf(5), savedFeature.getEstimatedEffort());
        assertEquals(TaskStatus.OPEN, savedFeature.getStatus());
        assertEquals(testUser.getId(), savedFeature.getAssignedUser().getId());
        assertNotNull(savedFeature.getDeadline(), "Deadline should be set");
    }

    @Test
    void shouldRetrieveFeatureTask() throws Exception {
        // Given
        TaskDto createdTask = createFeatureTask(testFeatureDto);

        // When & Then
        mockMvc.perform(get("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
                .andExpect(jsonPath("$.name").value("Add OAuth Support"))
                .andExpect(jsonPath("$.businessValue").value("High ROI potential"))
                .andExpect(jsonPath("$.estimatedEffort").value(5));
    }

    @Test
    void shouldUpdateFeatureTask() throws Exception {
        // Given
        TaskDto createdTask = createFeatureTask(testFeatureDto);
        
        FeatureDto updateDto = FeatureDto.builder()
                .id(createdTask.getId())
                .name("Add OAuth Support")
                .description("Implement OAuth authentication with Google")
                .assignedUserId(testUser.getId())
                .deadline(LocalDate.now().plusWeeks(3))
                .businessValue("Very high business impact")
                .estimatedEffort(8)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // When
        mockMvc.perform(put("/tasks/{id}", createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
                .andExpect(jsonPath("$.businessValue").value("Very high business impact"))
                .andExpect(jsonPath("$.estimatedEffort").value(8));

        // Then
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertTrue(savedTaskOpt.isPresent(), "Task should still exist after update");
        Task savedTask = savedTaskOpt.get();
        assertInstanceOf(Feature.class, savedTask, "Task should still be a Feature after update");
        Feature savedFeature = (Feature) savedTask;
        assertEquals("Add OAuth Support", savedFeature.getName());
        assertEquals("Implement OAuth authentication with Google", savedFeature.getDescription());
        assertEquals("Very high business impact", savedFeature.getBusinessValue());
        assertEquals(Integer.valueOf(8), savedFeature.getEstimatedEffort());
        assertEquals(TaskStatus.IN_PROGRESS, savedFeature.getStatus());
    }

    @Test
    void shouldDeleteFeatureTask() throws Exception {
        // Given
        TaskDto createdTask = createFeatureTask(testFeatureDto);

        // When
        mockMvc.perform(delete("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isNoContent());

        // Then
        // Verify task is deleted via API
        mockMvc.perform(get("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isNotFound());
                
        // Verify task is soft-deleted in database
        Optional<Task> savedTaskOpt = taskRepository.findById(createdTask.getId());
        assertFalse(savedTaskOpt.isPresent(), "Task should appear deleted through repository");
    }

    @Test
    void shouldListAllTasks() throws Exception {
        // Given
        TaskDto bugTask = createBugTask(testBugDto);
        TaskDto featureTask = createFeatureTask(testFeatureDto);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.hasPrevious").exists());
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        // Given
        String randomUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        UserDto filterUser = createTestUser(randomUsername);

        BugDto openBugDto = BugDto.builder()
                .name("Open Bug")
                .description("This is an open bug")
                .assignedUserId(filterUser.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.OPEN)
                .build();

        BugDto inProgressBugDto = BugDto.builder()
                .name("In Progress Bug")
                .description("This is an in-progress bug")
                .assignedUserId(filterUser.getId())
                .severity(BugSeverity.MEDIUM)
                .priority(BugPriority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        createBugTask(openBugDto);
        createBugTask(inProgressBugDto);

        // When & Then - Filter OPEN tasks
        mockMvc.perform(get("/tasks")
                .param("status", "OPEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("Open Bug"))
            .andExpect(jsonPath("$.items[0].status").value("OPEN"));

        // When & Then - Filter IN_PROGRESS tasks
        mockMvc.perform(get("/tasks")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("In Progress Bug"))
            .andExpect(jsonPath("$.items[0].status").value("IN_PROGRESS"));
    }

    @Test
    void shouldFilterTasksByAssignedUser() throws Exception {
        // Given
        String randomUsername1 = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String randomUsername2 = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        UserDto user1 = createTestUser(randomUsername1);
        UserDto user2 = createTestUser(randomUsername2);

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

        createBugTask(user1Bug);
        createBugTask(user2Bug);

        // When & Then - Filter tasks for user1
        mockMvc.perform(get("/tasks")
                .param("userId", user1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("User 1 Bug"))
            .andExpect(jsonPath("$.items[0].assignedUserId").value(user1.getId().toString()));

        // When & Then - Filter tasks for user2
        mockMvc.perform(get("/tasks")
                .param("userId", user2.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("User 2 Bug"))
            .andExpect(jsonPath("$.items[0].assignedUserId").value(user2.getId().toString()));
    }
}
