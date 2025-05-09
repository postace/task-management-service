package com.seneca.taskmanagement.dto;

import com.seneca.taskmanagement.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request object for updating a feature task")
public class UpdateFeatureRequest extends UpdateTaskRequest {

    @Schema(description = "Business value description", example = "High ROI potential with quick market adoption")
    private String businessValue;

    @Schema(description = "Deadline for the feature", example = "2024-12-31")
    private LocalDate deadline;

    @Schema(description = "Acceptance criteria for the feature")
    private String acceptanceCriteria;

    @Schema(description = "Estimated effort in story points", example = "5")
    private Integer estimatedEffort;

    @Override
    public String getTaskType() {
        return "FEATURE";
    }
}
