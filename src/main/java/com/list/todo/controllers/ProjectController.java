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
import com.list.todo.entity.User;
import com.list.todo.services.ProjectService;
import com.list.todo.services.UserService;

@RestController
@RequestMapping("/users/{userId}/projects")
public class ProjectController {

	private final ProjectService projectService;
	
	private final UserService userService;

	@Autowired
	public ProjectController(ProjectService projectService, UserService userService) {
		this.projectService = projectService;
		this.userService = userService;
	}

	@GetMapping
	public List<Project> getAllProjectsByUser(@PathVariable Long userId) {
		return projectService.findAllProjectsByUser(userId);
	}

	@GetMapping("/{id}")
	public Project getProject(@PathVariable Long id) {
		return projectService.findProject(id);
	}
	
	@PostMapping
	public void addProject(@RequestBody Project project, @PathVariable Long userId) {
		User user = userService.getUserById(userId);
		
		project.setId(userId);
		project.setUserOwnerId(userId);
		project.getUsers().add(user);

		user.getProjects().add(project);
		
		projectService.saveProject(project);
	}
	
	@PutMapping("/{id}")
	public void updateProject(@RequestBody Project project, @PathVariable Long userId) {
		project.setUserOwnerId(userId);
		project.getUsers().add(userService.getUserById(userId));
		projectService.updateProject(project);
	}
	
	@DeleteMapping("/{id}")
	public void deleteProject(@PathVariable Long id) {
		projectService.deleteProject(id);
	}
	
}
