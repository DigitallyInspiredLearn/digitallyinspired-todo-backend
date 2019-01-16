package com.list.todo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.Project;
import com.list.todo.entity.Task;
import com.list.todo.services.ProjectService;
import com.list.todo.services.TaskService;

@RestController
public class TasksController {

	@Autowired
	private TaskService taskService;
	
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping("/users/{userId}/projects/{projectId}/tasks")
	public List<Task> getAllTasksOnProject(@PathVariable Long projectId) {
		return taskService.getAllTasksOnProject(projectId);
	}

	@RequestMapping("/users/{userId}/projects/{projectId}/tasks/{id}")
	public Task getTask(@PathVariable Long id) {
		return taskService.getTask(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/users/{userId}/projects/{projectId}/tasks")
	public void addTask(@RequestBody Task task, @PathVariable Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		task.setProject(project);
		
		taskService.addTask(task);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/users/{userId}/projects/{projectId}/tasks/{id}")
	public void updateTask(@RequestBody Task task, @PathVariable Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		task.setProject(project);
		taskService.updateTask(task);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId}/projects/{projectId}/tasks/{id}")
	public void deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
	}
}
