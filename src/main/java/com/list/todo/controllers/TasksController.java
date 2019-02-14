package com.list.todo.controllers;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.InputTask;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TasksController {

	private final TaskService taskService;

	@GetMapping
	public ResponseEntity<List<Task>> getAllTasksOnTodoList(@RequestParam("todoListId") TodoList currentTodoList,
															@AuthenticationPrincipal UserPrincipal currentUser) {
		ResponseEntity<List<Task>> responseEntity;

		if (currentTodoList == null) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			List<Task> tasks = taskService.getAllTasksOnTodoList(currentTodoList.getId());
			responseEntity = new ResponseEntity<>(tasks, HttpStatus.OK);
		}
		return responseEntity;
	}

	@PostMapping
	public ResponseEntity<Task> addTask(@RequestBody InputTask inputTask,
										@RequestParam("todoListId") TodoList currentTodoList,
										@AuthenticationPrincipal UserPrincipal currentUser) {
		ResponseEntity<Task> responseEntity;

		if (currentTodoList == null) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			inputTask.setTodoListId(currentTodoList.getId());
			Task task = taskService.addTask(inputTask);

			responseEntity = new ResponseEntity<>(task, HttpStatus.OK);
		}
		return responseEntity;
	}

	@PutMapping("/{id}")
	public ResponseEntity<Task> updateTask(@RequestBody InputTask inputTask,
										   @PathVariable("id") Task currentTask,
										   @AuthenticationPrincipal UserPrincipal currentUser) {
		ResponseEntity<Task> responseEntity;
		TodoList currentTodoList;

		if (currentTask != null) {
			
			currentTodoList = currentTask.getTodoList();

			if (currentTodoList == null) {
				responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
				responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
			} else {
				currentTask = taskService.updateTask(currentTask.getId(), inputTask);
				responseEntity = new ResponseEntity<>(currentTask, HttpStatus.OK);
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
