package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private TodoListService todoListService;

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
        Optional<Task> newTask = Optional.of(new Task());

        if (todoList.isPresent()) {
//            newTask = todoList
//                    .map(tl -> {
//                        Task task = new Task();
//                        task.setBody(taskInput.getBody());
//                        task.setIsComplete(taskInput.getIsComplete());
//                        task.setTodoList(tl);
//                        return taskRepository.save(task);
//                    });
            Task task = new Task();
            task.setBody(taskInput.getBody());
            task.setIsComplete(taskInput.getIsComplete());
            task.setTodoList(todoList.get());
            newTask = Optional.of(taskRepository.save(task));
        }

        return newTask;
    }

    public Optional<Task> updateTask(Long currentTaskId, TaskInput taskInput) {

        return taskRepository.findById(currentTaskId).map(task -> {
            task.setBody(taskInput.getBody());
            task.setIsComplete(taskInput.getIsComplete());
            return taskRepository.save(task);
        });
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

}
