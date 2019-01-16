package com.list.todo.controllers;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.Project;
import com.list.todo.entity.User;
import com.list.todo.services.ProjectService;
import com.list.todo.services.UserService;

@RestController
public class ProjectController {

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/users/{userId}/projects")
	public List<Project> getAllProjectsByUser(@PathVariable Long userId) {
		return projectService.findAllProjectsByUser(userId);
	}

	@RequestMapping("/users/{userId}/projects/{id}")
	public Project getProject(@PathVariable Long id) {
		return projectService.findProject(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/users/{userId}/projects")
	public void addProject(@RequestBody Project project, @PathVariable Long userId) {
		User user = userService.getUserById(userId);
		
		project.setUserOwnerId(userId);
		project.setUsers(new HashSet<User>() {{
			add(user);
		}});
		
		if (user.getProjects() == null) {
			user.setProjects(new HashSet<Project>() {{
				add(project);
			}});
		} else {
			user.getProjects().add(project);
		}
		
		projectService.saveProject(project);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/users/{userId}/projects/{id}")
	public void updateProject(@RequestBody Project project, @PathVariable Long userId) {
		project.setUserOwnerId(userId);
		project.getUsers().add(userService.getUserById(userId));
		projectService.updateProject(project);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId}/projects/{id}")
	public void deleteProject(@PathVariable Long id) {
		projectService.deleteProject(id);
	}
	
}
