package com.list.todo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.Task;
import com.list.todo.services.TodoListService;
import com.list.todo.services.TaskService;

@RestController
@RequestMapping("/api/todolists/{todoListId}/tasks")
public class TasksController {

	private final TaskService taskService;
	
	@Autowired
	public TasksController(TaskService taskService, TodoListService todoListService) {
		this.taskService = taskService;
	}

	@GetMapping
	public List<Task> getAllTasksOnTodoList(@PathVariable Long todoListId) {
		return taskService.getAllTasksOnTodoList(todoListId);
	}

	@GetMapping("/{id}")
	public Task getTask(@PathVariable Long id) {
		return taskService.getTask(id);
	}
	
	@PostMapping
	public void addTask(@RequestBody Task task, @PathVariable Long todoListId) {
		TodoList todoList = new TodoList();
		todoList.setId(todoListId);
		task.setTodoList(todoList);
		
		taskService.addTask(task);
	}
	
	@PutMapping("/{id}")
	public void updateTask(@RequestBody Task task, @PathVariable Long id, @PathVariable Long todoListId) {
		TodoList todoList = new TodoList();
		todoList.setId(todoListId);
		task.setId(id);
		task.setTodoList(todoList);
		
		taskService.updateTask(task);
	}
	
	@DeleteMapping("/{id}")
	public void deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
	}
}
