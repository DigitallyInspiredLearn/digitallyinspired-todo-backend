package com.list.todo.controllers;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
import com.list.todo.services.TodoListService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TasksController {

    private final TaskService taskService;
    private final TodoListService todoListService;

    @GetMapping
    public ResponseEntity<Iterable<Task>> getAllTasksOnTodoList(@RequestParam("todoListId") Long todoListId,
                                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Iterable<Task>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Iterable<Task> tasks = taskService.getAllTasksOnTodoList(todoList.get().getId());
            responseEntity = new ResponseEntity<>(tasks, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping
    public ResponseEntity<Optional<Task>> addTask(@RequestBody TaskInput taskInput,
                                                  @RequestParam("todoListId") Long todoListId,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<Task>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            taskInput.setTodoListId(todoList.get().getId());
            Optional<Task> task = taskService.addTask(taskInput);
            responseEntity = new ResponseEntity<>(task, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Task>> updateTask(@RequestBody TaskInput taskInput,
                                                     @PathVariable("id") Long taskId,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<Task>> responseEntity;
        Optional<Task> task = taskService.getTaskById(taskId);

        if (task.isPresent()) {

            TodoList todoList = task.get().getTodoList();

            if (todoList == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (!todoList.getCreatedBy().equals(currentUser.getUsername())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                Optional<Task> updatedTask = taskService.updateTask(task.get().getId(), taskInput);
                responseEntity = new ResponseEntity<>(updatedTask, HttpStatus.OK);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long taskId,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Void> responseEntity;
        Optional<Task> task = taskService.getTaskById(taskId);

        if (task.isPresent()) {
            TodoList todoList = task.get().getTodoList();

            if (todoList == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (!todoList.getCreatedBy().equals(currentUser.getUsername())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                taskService.deleteTask(task.get().getId());
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {

            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
}
