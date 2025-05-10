package com.seneca.taskmanagement.dto;

import com.seneca.taskmanagement.domain.Bug.BugSeverity;
import com.seneca.taskmanagement.domain.Bug.BugPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bug creation request")
public class CreateBugDto extends CreateTaskDto {

    @NotNull(message = "Bug severity is required")
    @Schema(description = "Severity level of the bug", example = "HIGH")
    private BugSeverity severity;

    @Schema(description = "Steps to reproduce the bug", example = "1. Login 2. Navigate to profile 3. Click edit")
    private String stepsToReproduce;

    @NotNull(message = "Bug priority is required")
    @Schema(description = "Priority level of the bug", example = "HIGH")
    private BugPriority priority;

    @Schema(description = "Environment where the bug was found", example = "Production")
    private String environment;

    @Override
    public String getTaskType() {
        return "BUG";
    }
}
