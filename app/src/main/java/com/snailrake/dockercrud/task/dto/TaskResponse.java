package com.snailrake.dockercrud.task.dto;

import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
