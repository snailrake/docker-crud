package com.snailrake.dockercrud.task.dto;

import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 120, message = "Title must not be longer than 120 characters")
        String title,
        @Size(max = 1000, message = "Description must not be longer than 1000 characters")
        String description,
        @NotNull(message = "Priority must not be null")
        Priority priority,
        @NotNull(message = "Status must not be null")
        TaskStatus status,
        LocalDate dueDate
) {
}
