package com.seneca.taskmanagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("BUG")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Bug extends Task {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugPriority priority;

    @Column(length = 100)
    private String environment;

    public enum BugSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum BugPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}
