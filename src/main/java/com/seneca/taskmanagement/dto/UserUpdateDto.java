package com.seneca.taskmanagement.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    
    @Size(min = 3, max = 100)
    private String fullName;
}
