package com.seneca.taskmanagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("FEATURE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Feature extends Task {

    @Column(columnDefinition = "TEXT")
    private String businessValue;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(columnDefinition = "TEXT")
    private String acceptanceCriteria;

    private Integer estimatedEffort;
}
