package com.seneca.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.seneca.taskmanagement.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "taskType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateBugRequest.class, name = "BUG"),
        @JsonSubTypes.Type(value = UpdateFeatureRequest.class, name = "FEATURE")
})
@Data
@Schema(description = "Base request object for updating a task")
public abstract class UpdateTaskRequest {
    @Schema(description = "Name of the task")
    private String name;

    @Schema(description = "Description of the task")
    private String description;

    @Schema(description = "ID of the user assigned to this task")
    private UUID assignedUserId;

    @Schema(description = "Current status of the task")
    private TaskStatus status;

    public abstract String getTaskType();
}
