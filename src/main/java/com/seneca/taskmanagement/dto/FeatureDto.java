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

    @NotNull(message = "Business value is required")
    @Min(value = 1, message = "Business value must be at least 1")
    @Max(value = 10, message = "Business value must not exceed 10")
    @Schema(description = "Business value on a scale of 1-10", example = "8")
    private Integer businessValue;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    @Schema(description = "Deadline for the feature implementation", example = "2024-01-15")
    private LocalDate deadline;

    @Override
    public String getTaskType() {
        return "FEATURE";
    }
}
