package com.seneca.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.seneca.taskmanagement.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "taskType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateBugDto.class, name = "BUG"),
        @JsonSubTypes.Type(value = CreateFeatureDto.class, name = "FEATURE")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base task creation request", subTypes = {CreateBugDto.class, CreateFeatureDto.class})
public abstract class CreateTaskDto {

    @NotBlank(message = "Task name is required")
    @Size(max = 100)
    @Schema(description = "Name of the task", example = "Implement login functionality")
    private String name;

    @Schema(description = "Detailed description of the task")
    private String description;

    @Schema(description = "ID of the user assigned to this task")
    private UUID assignedUserId;

    @Schema(description = "Status of the task", example = "OPEN")
    private TaskStatus status;

    @Schema(description = "Type discriminator for the task")
    public abstract String getTaskType();
}
