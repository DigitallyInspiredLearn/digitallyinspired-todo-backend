package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final TodoListService todoListService;
    private final TagTaskKeyService tagTaskKeyService;

    public Optional<Task> getTaskById(Long currentTaskId) {
        return taskRepository.findById(currentTaskId);
    }

    Long countTasksByCreatedBy(String createdBy) {
        return taskRepository.countByCreatedBy(createdBy);
    }

    Long countTasksByCreatedByAndIsComplete(String createdBy, Boolean isComplete) {
        return taskRepository.countByCreatedByAndIsComplete(createdBy, isComplete);
    }

    public Iterable<Task> getAllTasksOnTodoList(Long todoListId) {

        List<Task> tasks = new ArrayList<>();

        todoListService.getTodoListById(todoListId).ifPresent(todoList ->
                tasks.addAll(taskRepository.findTasksByTodoListIdOrderByPriority(todoListId)));

        return tasks;
    }

    public Optional<Task> addTask(TaskInput taskInput) {

        AtomicReference<Optional<Task>> newTask = new AtomicReference<>(Optional.empty());

        todoListService.getTodoListById(taskInput.getTodoListId()).ifPresent(todoList -> {
            Task task = Task.builder()
                    .body(taskInput.getBody())
                    .isComplete(taskInput.getIsComplete())
                    .priority(taskInput.getPriority())
                    .todoList(todoList)
                    .build();
            newTask.set(Optional.of(taskRepository.save(task)));
        });

        return newTask.get();
    }

    public Optional<Task> updateTask(Long currentTaskId, TaskInput taskInput) {
        // Нет проверки на существование todolist у таска
        return taskRepository.findById(currentTaskId)
                .map(task -> {
                    task.setBody(taskInput.getBody());
                    if (taskInput.getIsComplete() && !task.getIsComplete()) {
                        task.setCompletedDate(System.currentTimeMillis());
                    }
                    task.setDurationTime(taskInput.getDurationTime());
                    task.setIsComplete(taskInput.getIsComplete());
                    task.setPriority(taskInput.getPriority());
                    return taskRepository.save(task);
                });
    }

    public void deleteTask(Long taskId) {
        tagTaskKeyService.deleteTaggedTask(taskId);
        taskRepository.deleteById(taskId);
    }

}
