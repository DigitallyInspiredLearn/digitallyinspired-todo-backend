package com.list.todo.controllers;

import java.util.Set;

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
import com.list.todo.entity.User;
import com.list.todo.security.CurrentUser;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;

@RestController
@RequestMapping("/api/todolists")
public class TodoListController {

	private final TodoListService todoListService;
	
	private final UserService userService;

	@Autowired
	public TodoListController(TodoListService todoListService, UserService userService) {
		this.todoListService = todoListService;
		this.userService = userService;
	}

	@GetMapping
	public Set<TodoList> getAllTodoListsByUser(@CurrentUser UserPrincipal currentUser) {
		Long userId = currentUser.getId();
		return todoListService.getAllTodoListsByUser(userId);
	}

	@GetMapping("/{id}")
	public TodoList getTodoList(@PathVariable Long id) {
		return todoListService.getTodoList(id);
	}
	
	@PostMapping
	public void addTodoList(@RequestBody TodoList todoList, @CurrentUser UserPrincipal currentUser) {
		Long userId = currentUser.getId();
		User user = userService.getUserById(userId);
		
		todoList.setUserOwnerId(userId);
		todoList.getUsers().add(user);

		user.getTodoLists().add(todoList);
		userService.updateUser(user);
		
		todoListService.addTodoList(todoList);;
	}
	
	@PutMapping("/{id}")
	public void updateTodoList(@RequestBody TodoList todoList, @CurrentUser UserPrincipal currentUser) {
		//TODO: доделать обновление 
		todoListService.updateTodoList(todoList);
	}
	
	@DeleteMapping("/{id}")
	public void deleteTodoList(@CurrentUser UserPrincipal currentUser) {
		Long userId = currentUser.getId();
		todoListService.deleteTodoList(userId);
	}
	
}
