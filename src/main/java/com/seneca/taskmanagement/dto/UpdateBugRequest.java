package com.seneca.taskmanagement.dto;

import com.seneca.taskmanagement.domain.Bug.BugPriority;
import com.seneca.taskmanagement.domain.Bug.BugSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request object for updating a bug task")
public class UpdateBugRequest extends UpdateTaskRequest {
    @Schema(description = "Severity level of the bug")
    private BugSeverity severity;

    @Schema(description = "Priority level of the bug")
    private BugPriority priority;

    @Schema(description = "Steps to reproduce the bug")
    private String stepsToReproduce;

    @Schema(description = "Environment where the bug occurs")
    private String environment;

    @Override
    public String getTaskType() {
        return "BUG";
    }
}
