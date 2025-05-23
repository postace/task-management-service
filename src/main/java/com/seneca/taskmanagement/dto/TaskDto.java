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

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "taskType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BugDto.class, name = "BUG"),
        @JsonSubTypes.Type(value = FeatureDto.class, name = "FEATURE")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Abstract base task representation", subTypes = {BugDto.class, FeatureDto.class})
public abstract class TaskDto {

    @Schema(description = "Unique identifier of the task", example = "1")
    private UUID id;

    @Schema(description = "Name of the task", example = "Implement login functionality")
    private String name;

    @Schema(description = "Detailed description of the task")
    private String description;

    @Schema(description = "Creation timestamp", example = "2023-12-01T10:15:30+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2023-12-01T10:15:30+07:00")
    private OffsetDateTime updatedAt;

    @Schema(description = "Status of the task", example = "OPEN")
    private TaskStatus status;

    @Schema(description = "ID of the user assigned to this task")
    private UUID assignedUserId;

    @Schema(description = "Type discriminator for the task")
    public abstract String getTaskType();
}
