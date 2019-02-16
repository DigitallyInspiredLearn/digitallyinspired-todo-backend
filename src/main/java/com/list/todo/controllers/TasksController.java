package com.list.todo.controllers;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
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

    @GetMapping
    public ResponseEntity<Iterable<Task>> getAllTasksOnTodoList(@RequestParam("todoListId") TodoList currentTodoList,
                                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Iterable<Task>> responseEntity;

        if (currentTodoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Iterable<Task> tasks = taskService.getAllTasksOnTodoList(currentTodoList.getId());
            responseEntity = new ResponseEntity<>(tasks, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping
    public ResponseEntity<Optional<Task>> addTask(@RequestBody TaskInput taskInput,
                                                  @RequestParam("todoListId") TodoList currentTodoList,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<Task>> responseEntity;

        if (currentTodoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            taskInput.setTodoListId(currentTodoList.getId());
            Optional<Task> task = taskService.addTask(taskInput);
            responseEntity = new ResponseEntity<>(task, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Task>> updateTask(@RequestBody TaskInput taskInput,
                                                     @PathVariable("id") Task currentTask,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<Task>> responseEntity;
        TodoList currentTodoList;

        if (currentTask != null) {

            currentTodoList = currentTask.getTodoList();

            if (currentTodoList == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                Optional<Task> updatedTask = taskService.updateTask(currentTask.getId(), taskInput);
                responseEntity = new ResponseEntity<>(updatedTask, HttpStatus.OK);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Task task,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Void> responseEntity;
        TodoList currentTodoList;

        if (task != null) {

            currentTodoList = task.getTodoList();

            if (currentTodoList == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                taskService.deleteTask(task.getId());
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {

            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
}
