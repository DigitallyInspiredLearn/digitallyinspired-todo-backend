package com.list.todo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.security.CurrentUser;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
import com.list.todo.services.TodoListService;

@RestController
@RequestMapping("/api/todolists/{todoListId}/tasks")
public class TasksController {

	private final TaskService taskService;
	private final TodoListService todoListService;
	
	@Autowired
	public TasksController(TaskService taskService, TodoListService todoListService) {
		this.taskService = taskService;
		this.todoListService = todoListService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<Task>> getAllTasksOnTodoList(@PathVariable Long todoListId,
															@CurrentUser UserPrincipal currentUser) {
		TodoList currentTodoList = todoListService.getTodoList(todoListId);

		if (currentTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		List<Task> tasks = taskService.getAllTasksOnTodoList(todoListId);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Task> getTask(@PathVariable Long id,
										@PathVariable Long todoListId,
										@CurrentUser UserPrincipal currentUser) {
		TodoList currentTodoList = todoListService.getTodoList(todoListId);
		Task task = taskService.getTask(id);

		if (currentTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (task == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(task, HttpStatus.OK);
	}
	
	@PostMapping	
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Void> addTask(@RequestBody Task task,
										@PathVariable Long todoListId,
										@CurrentUser UserPrincipal currentUser) {
		TodoList currentTodoList = todoListService.getTodoList(todoListId);
		
		if (currentTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		task.setTodoListId(todoListId);
		task.setIsComplete(false);
		
		taskService.addTask(task);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Task> updateTask(@RequestBody Task task,
										   @PathVariable Long id,
										   @PathVariable Long todoListId,
										   @CurrentUser UserPrincipal currentUser) {
		TodoList currentTodoList = todoListService.getTodoList(todoListId);
		TodoList editTodoList = todoListService.getTodoList(task.getTodoListId());		

		if (currentTodoList == null || editTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())
				|| (!editTodoList.getUserOwnerId().equals(currentUser.getId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		Task currentTask = taskService.getTask(id);
		currentTask.setBody(task.getBody());
		currentTask.setIsComplete(task.getIsComplete());
		currentTask.setTodoListId(task.getTodoListId());
		
		taskService.updateTask(currentTask);

		return new ResponseEntity<>(currentTask, HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id,
										   @PathVariable Long todoListId,
										   @CurrentUser UserPrincipal currentUser) {
		TodoList currentTodoList = todoListService.getTodoList(todoListId);

		if (currentTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		taskService.deleteTask(id);

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
