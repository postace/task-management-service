package com.seneca.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User representation for API interaction")
public class UserDto {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @NotBlank(message = "Username is required")
    @Schema(description = "Unique username", example = "jdoe")
    private String username;

    @NotBlank(message = "Full name is required")
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Timestamp when the user was created", example = "2025-05-08T10:15:30")
    private LocalDateTime createdAt;
}
