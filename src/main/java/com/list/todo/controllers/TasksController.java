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

import com.list.todo.entity.Project;
import com.list.todo.entity.Task;
import com.list.todo.services.ProjectService;
import com.list.todo.services.TaskService;

@RestController
@RequestMapping("/users/{userId}/projects/{projectId}/tasks")
public class TasksController {

	private final TaskService taskService;
	
	@Autowired
	public TasksController(TaskService taskService, ProjectService projectService) {
		this.taskService = taskService;
	}

	@GetMapping
	public List<Task> getAllTasksOnProject(@PathVariable Long projectId) {
		return taskService.getAllTasksOnProject(projectId);
	}

	@GetMapping("/{id}")
	public Task getTask(@PathVariable Long id) {
		return taskService.getTask(id);
	}
	
	@PostMapping
	public void addTask(@RequestBody Task task, @PathVariable Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		task.setProject(project);
		
		taskService.addTask(task);
	}
	
	@PutMapping("/{id}")
	public void updateTask(@RequestBody Task task, @PathVariable Long id, @PathVariable Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		task.setId(id);
		task.setProject(project);
		
		taskService.updateTask(task);
	}
	
	@DeleteMapping("/{id}")
	public void deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
	}
}
