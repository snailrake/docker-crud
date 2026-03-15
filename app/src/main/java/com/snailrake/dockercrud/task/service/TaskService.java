package com.snailrake.dockercrud.task.service;

import com.snailrake.dockercrud.task.dto.TaskCreateRequest;
import com.snailrake.dockercrud.task.dto.TaskResponse;
import com.snailrake.dockercrud.task.dto.TaskUpdateRequest;
import com.snailrake.dockercrud.task.exception.TaskNotFoundException;
import com.snailrake.dockercrud.task.mapper.TaskMapper;
import com.snailrake.dockercrud.task.model.Priority;
import com.snailrake.dockercrud.task.model.Task;
import com.snailrake.dockercrud.task.model.TaskStatus;
import com.snailrake.dockercrud.task.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class TaskService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id",
            "title",
            "priority",
            "status",
            "dueDate",
            "createdAt",
            "updatedAt"
    );

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAll(TaskStatus status, Priority priority, String search, String sortBy, String direction) {
        Specification<Task> specification = Specification.where(hasStatus(status))
                .and(hasPriority(priority))
                .and(containsSearch(search));

        Sort sort = buildSort(sortBy, direction);
        return taskRepository.findAll(specification, sort)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    public TaskResponse getById(Long id) {
        return TaskMapper.toResponse(findTask(id));
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(normalize(request.description()));
        task.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        task.setStatus(request.status() != null ? request.status() : TaskStatus.PLANNED);
        task.setDueDate(request.dueDate());
        return TaskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title().trim());
        task.setDescription(normalize(request.description()));
        task.setPriority(request.priority());
        task.setStatus(request.status());
        task.setDueDate(request.dueDate());
        return TaskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private Sort buildSort(String sortBy, String direction) {
        String resolvedSortBy = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        if (!ALLOWED_SORT_FIELDS.contains(resolvedSortBy)) {
            throw new IllegalArgumentException("Unsupported sort field: " + resolvedSortBy);
        }

        Sort.Direction resolvedDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(resolvedDirection, resolvedSortBy);
    }

    private Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<Task> hasPriority(Priority priority) {
        return (root, query, criteriaBuilder) ->
                priority == null ? null : criteriaBuilder.equal(root.get("priority"), priority);
    }

    private Specification<Task> containsSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
