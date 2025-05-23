package com.seneca.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Feature task representation")
public class FeatureDto extends TaskDto {

    @Schema(description = "Business value description", example = "High ROI potential with quick market adoption")
    private String businessValue;

    @Schema(description = "Deadline for the feature implementation", example = "2024-01-15")
    private LocalDate deadline;

    @Schema(description = "Acceptance criteria for the feature", example = "1. User can login with email\n2. Password reset functionality works")
    private String acceptanceCriteria;

    @Schema(description = "Estimated effort in story points", example = "5")
    private Integer estimatedEffort;

    @Override
    public String getTaskType() {
        return "FEATURE";
    }
}
