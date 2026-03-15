package com.snailrake.dockercrud.task.service;

import com.snailrake.dockercrud.task.dto.TaskCreateRequest;
import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.Task;
import com.snailrake.dockercrud.task.model.TaskStatus;
import com.snailrake.dockercrud.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldApplyDefaultPriorityAndStatusWhenCreatingTask() {
        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Prepare Docker setup");
        savedTask.setDescription("Create docker-compose and Dockerfiles");
        savedTask.setPriority(Priority.MEDIUM);
        savedTask.setStatus(TaskStatus.PLANNED);
        savedTask.setDueDate(LocalDate.of(2026, 3, 20));

        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class))).thenReturn(savedTask);

        TaskCreateRequest request = new TaskCreateRequest(
                "Prepare Docker setup",
                "Create docker-compose and Dockerfiles",
                null,
                null,
                LocalDate.of(2026, 3, 20)
        );

        TaskResponse response = taskService.create(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task taskToSave = taskCaptor.getValue();

        assertThat(taskToSave.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(taskToSave.getStatus()).isEqualTo(TaskStatus.PLANNED);
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.PLANNED);
    }
}
