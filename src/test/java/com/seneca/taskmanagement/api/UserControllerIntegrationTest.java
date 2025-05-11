package com.seneca.taskmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seneca.taskmanagement.config.TestContainersConfig;
import com.seneca.taskmanagement.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class UserControllerIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCrudOperations() throws Exception {
        // Create a user
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .fullName("Test User")
                .build();

        // Test Create User
        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andReturn();

        // Extract created user ID
        UserDto createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                UserDto.class);
        UUID userId = createdUser.getId();
        assertNotNull(userId);

        // Test Get User by ID
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        // Test Update User
        UserDto updateDto = UserDto.builder()
                .id(userId)
                .username("testuser") // Keep the same username
                .fullName("Updated User")
                .build();

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Updated User"));

        // Test Get All Users
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(userId.toString()));

        // Test Delete User
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
