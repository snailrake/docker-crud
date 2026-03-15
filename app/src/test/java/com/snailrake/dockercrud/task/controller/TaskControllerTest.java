package com.snailrake.dockercrud.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snailrake.dockercrud.task.dto.TaskCreateRequest;
import com.snailrake.dockercrud.task.dto.TaskResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        TaskResponse response = new TaskResponse(
                1L,
                "Write service layer",
                "Implement business logic and tests",
                Priority.HIGH,
                TaskStatus.IN_PROGRESS,
                LocalDate.of(2026, 3, 25),
                OffsetDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)
        );

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
}
