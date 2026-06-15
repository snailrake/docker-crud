package com.snailrake.dockercrud.task.mapper;

import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.Task;
import com.snailrake.dockercrud.task.model.TaskStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    @Test
    void shouldMapEntityToResponse() {
        OffsetDateTime timestamp = OffsetDateTime.of(2026, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        Task task = new Task();
        task.setId(11L);
        task.setTitle("Map task");
        task.setDescription("Convert entity to dto");
        task.setPriority(Priority.MEDIUM);
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDueDate(LocalDate.of(2026, 3, 27));
        task.setCreatedAt(timestamp);
        task.setUpdatedAt(timestamp);

        TaskResponse response = TaskMapper.toResponse(task);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.title()).isEqualTo("Map task");
        assertThat(response.description()).isEqualTo("Convert entity to dto");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 3, 27));
        assertThat(response.createdAt()).isEqualTo(timestamp);
        assertThat(response.updatedAt()).isEqualTo(timestamp);
    }
}
