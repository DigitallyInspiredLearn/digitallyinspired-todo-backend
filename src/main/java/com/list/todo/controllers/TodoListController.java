package com.list.todo.controllers;

import java.util.Set;

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

import com.list.todo.entity.TodoList;
import com.list.todo.security.CurrentUser;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TodoListService;

@RestController
@RequestMapping("/api/todolists")
public class TodoListController {

	private final TodoListService todoListService;
	

	@Autowired
	public TodoListController(TodoListService todoListService) {
		this.todoListService = todoListService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Set<TodoList>> getAllTodoListsByUser(@CurrentUser UserPrincipal currentUser) {
		Long userId = currentUser.getId();

		return new ResponseEntity<>(todoListService.getAllTodoListsByUser(userId), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<TodoList> getTodoList(@PathVariable Long id,
												@CurrentUser UserPrincipal currentUser) {
		TodoList todoList = todoListService.getTodoList(id);

		if (todoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (!todoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(todoList, HttpStatus.OK);
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public void addTodoList(@RequestBody TodoList todoList,
							@CurrentUser UserPrincipal currentUser) {
		Long userId = currentUser.getId();
		todoList.setUserOwnerId(userId);
		todoListService.addTodoList(todoList);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<TodoList> updateTodoList(@RequestBody TodoList todoList,
												   @CurrentUser UserPrincipal currentUser,
												   @PathVariable Long id) {
		TodoList currentTodoList = todoListService.getTodoList(id);

		if (currentTodoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		currentTodoList.setTodoListName(todoList.getTodoListName());
		todoListService.updateTodoList(currentTodoList);

		return new ResponseEntity<>(currentTodoList, HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Void> deleteTodoList(@CurrentUser UserPrincipal currentUser,
											   @PathVariable Long id) {
		TodoList todoList = todoListService.getTodoList(id);

		if (todoList == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (!todoList.getUserOwnerId().equals(currentUser.getId())){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		todoListService.deleteTodoList(id);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
