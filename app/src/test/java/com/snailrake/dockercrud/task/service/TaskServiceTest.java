package com.snailrake.dockercrud.task.service;

import com.snailrake.dockercrud.task.dto.TaskCreateRequest;
import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.dto.TaskUpdateRequest;
import com.snailrake.dockercrud.task.exception.TaskNotFoundException;
import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.Task;
import com.snailrake.dockercrud.task.model.TaskStatus;
import com.snailrake.dockercrud.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void shouldReturnTasksUsingDefaultSort() {
        Task task = buildTask(7L, "Docker", "Run compose", Priority.HIGH, TaskStatus.IN_PROGRESS);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), any(Sort.class))).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAll(null, null, "doc", null, null);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), sortCaptor.capture());

        Sort.Order createdAtOrder = sortCaptor.getValue().getOrderFor("createdAt");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(7L);
        assertThat(createdAtOrder).isNotNull();
        assertThat(createdAtOrder.isAscending()).isTrue();
    }

    @Test
    void shouldThrowWhenUnsupportedSortFieldRequested() {
        assertThatThrownBy(() -> taskService.getAll(null, null, null, "unsupportedField", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort field: unsupportedField");
    }

    @Test
    void shouldReturnTaskById() {
        Task task = buildTask(2L, "Inspect API", "Call GET endpoint", Priority.LOW, TaskStatus.PLANNED);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(2L);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.title()).isEqualTo("Inspect API");
        assertThat(response.status()).isEqualTo(TaskStatus.PLANNED);
    }

    @Test
    void shouldUpdateTaskAndNormalizeDescription() {
        Task task = buildTask(3L, "Old title", "Old description", Priority.LOW, TaskStatus.PLANNED);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskUpdateRequest request = new TaskUpdateRequest(
                "  Updated title  ",
                "   ",
                Priority.HIGH,
                TaskStatus.DONE,
                LocalDate.of(2026, 3, 30)
        );

        TaskResponse response = taskService.update(3L, request);

        assertThat(task.getTitle()).isEqualTo("Updated title");
        assertThat(task.getDescription()).isNull();
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldDeleteTaskWhenItExists() {
        Task task = buildTask(4L, "Delete task", null, Priority.MEDIUM, TaskStatus.PLANNED);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));

        taskService.delete(4L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowWhenTaskIsMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id 99 was not found");
    }

    private Task buildTask(Long id, String title, String description, Priority priority, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setDueDate(LocalDate.of(2026, 3, 25));
        return task;
    }
}
