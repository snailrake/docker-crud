package com.snailrake.dockercrud.task.mapper;

import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.model.Task;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
