package com.snailrake.dockercrud.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snailrake.dockercrud.task.dto.TaskCreateRequest;
import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.dto.TaskUpdateRequest;
import com.snailrake.dockercrud.task.exception.TaskNotFoundException;
import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.TaskStatus;
import com.snailrake.dockercrud.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void shouldCreateTask() throws Exception {
        TaskResponse response = buildResponse(1L, "Write service layer", TaskStatus.IN_PROGRESS, Priority.HIGH);

        when(taskService.create(any(TaskCreateRequest.class))).thenReturn(response);

        TaskCreateRequest request = new TaskCreateRequest(
                "Write service layer",
                "Implement business logic and tests",
                Priority.HIGH,
                TaskStatus.IN_PROGRESS,
                LocalDate.of(2026, 3, 25)
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write service layer"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        when(taskService.getAll(TaskStatus.DONE, Priority.HIGH, "docker", "dueDate", "desc"))
                .thenReturn(List.of(buildResponse(5L, "Finish task", TaskStatus.DONE, Priority.HIGH)));

        mockMvc.perform(get("/api/tasks")
                        .param("status", "DONE")
                        .param("priority", "HIGH")
                        .param("search", "docker")
                        .param("sortBy", "dueDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("Finish task"))
                .andExpect(jsonPath("$[0].status").value("DONE"));

        verify(taskService).getAll(TaskStatus.DONE, Priority.HIGH, "docker", "dueDate", "desc");
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Update task",
                "Refresh description",
                Priority.MEDIUM,
                TaskStatus.DONE,
                LocalDate.of(2026, 3, 28)
        );

        when(taskService.update(eq(2L), any(TaskUpdateRequest.class)))
                .thenReturn(buildResponse(2L, "Update task", TaskStatus.DONE, Priority.MEDIUM));

        mockMvc.perform(put("/api/tasks/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).delete(3L);
    }

    @Test
    void shouldReturnValidationErrorWhenRequestIsInvalid() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                " ",
                "Broken request",
                Priority.LOW,
                TaskStatus.PLANNED,
                LocalDate.of(2026, 3, 25)
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0]").value("title: Title must not be blank"));
    }

    @Test
    void shouldReturnNotFoundWhenTaskIsMissing() throws Exception {
        when(taskService.getById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.details[0]").value("Task with id 99 was not found"));
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException() throws Exception {
        when(taskService.getAll(null, null, null, "wrongField", null))
                .thenThrow(new IllegalArgumentException("Unsupported sort field: wrongField"));

        mockMvc.perform(get("/api/tasks")
                        .param("sortBy", "wrongField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0]").value("Unsupported sort field: wrongField"));
    }

    private TaskResponse buildResponse(Long id, String title, TaskStatus status, Priority priority) {
        return new TaskResponse(
                id,
                title,
                "Implement business logic and tests",
                priority,
                status,
                LocalDate.of(2026, 3, 25),
                OffsetDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)
        );
    }
}
