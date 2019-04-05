package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final TodoListService todoListService;

    public Optional<Task> getTaskById(Long currentTaskId) {
        return taskRepository.findById(currentTaskId);
    }

    public Iterable<Task> getAllTasksOnTodoList(Long todoListId) {
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);
        Iterable<Task> tasks = null;

        if (todoList.isPresent()) {
            tasks = taskRepository.findTasksByTodoListId(todoListId);
        }

        return tasks;
    }

    public Optional<Task> addTask(TaskInput taskInput) {

        Optional<TodoList> todoList = todoListService.getTodoListById(taskInput.getTodoListId());
        Optional<Task> newTask = Optional.empty();

        if (todoList.isPresent()) {
            Task task = Task.builder()
                    .body(taskInput.getBody())
                    .isComplete(taskInput.getIsComplete())
                    .todoList(todoList.get())
                    .build();
            newTask = Optional.of(taskRepository.save(task));

        }

        return newTask;
    }

    public Optional<Task> updateTask(Long currentTaskId, TaskInput taskInput) {
        // Нет проверки на существование todolist у таска
        return taskRepository.findById(currentTaskId)
                .map(task -> {
                    task.setBody(taskInput.getBody());
                    task.setIsComplete(taskInput.getIsComplete());
                    if (taskInput.getIsComplete()){
                        task.setCompletedDate(System.currentTimeMillis());
                        task.setRealizationTime(task.getCompletedDate()-task.getCreatedDate());
                    }
                    return taskRepository.save(task);
                });
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

}
