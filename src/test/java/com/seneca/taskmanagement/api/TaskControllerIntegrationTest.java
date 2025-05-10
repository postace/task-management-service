package com.seneca.taskmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seneca.taskmanagement.config.TestContainersConfig;
import com.seneca.taskmanagement.dto.BugDto;
import com.seneca.taskmanagement.dto.FeatureDto;
import com.seneca.taskmanagement.dto.TaskDto;
import com.seneca.taskmanagement.dto.UserDto;
import com.seneca.taskmanagement.domain.Bug.BugSeverity;
import com.seneca.taskmanagement.domain.Bug.BugPriority;
import com.seneca.taskmanagement.domain.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        // Test Get All Tasks
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andDo(print())
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
    }
}
