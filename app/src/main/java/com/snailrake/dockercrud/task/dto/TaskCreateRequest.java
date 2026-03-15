package com.snailrake.dockercrud.task.dto;

import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 120, message = "Title must not be longer than 120 characters")
        String title,
        @Size(max = 1000, message = "Description must not be longer than 1000 characters")
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate
) {
}
