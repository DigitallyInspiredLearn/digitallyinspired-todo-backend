package com.list.todo.controllers;

import com.list.todo.entity.TodoList;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TodoListService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/todolists")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TodoListController {

	private final TodoListService todoListService;

	@GetMapping
	public ResponseEntity<Set<TodoList>> getAllTodoListsByUser(@AuthenticationPrincipal UserPrincipal currentUser) {
		Long userId = currentUser.getId();

		return new ResponseEntity<>(todoListService.getAllTodoListsByUser(userId), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TodoList> getTodoList(@PathVariable("id") TodoList todoList,
												@AuthenticationPrincipal UserPrincipal currentUser) {
		ResponseEntity<TodoList> responseEntity;
		if (todoList == null){
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else if (!todoList.getUserOwnerId().equals(currentUser.getId())){
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			responseEntity = new ResponseEntity<>(todoList, HttpStatus.OK);
		}

		return responseEntity;
	}
	
	@PostMapping
	public ResponseEntity<TodoList> addTodoList(@RequestBody TodoList todoList,
												@AuthenticationPrincipal UserPrincipal currentUser) {
		Long userId = currentUser.getId();

		todoList.setUserOwnerId(userId);

		todoListService.addTodoList(todoList);

		return new ResponseEntity<>(todoList, HttpStatus.OK);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<TodoList> updateTodoList(@RequestBody TodoList todoList,
												   @AuthenticationPrincipal UserPrincipal currentUser,
												   @PathVariable("id") TodoList currentTodoList) {
		ResponseEntity<TodoList> responseEntity;

		if (currentTodoList == null){
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())){
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			BeanUtils.copyProperties(todoList, currentTodoList, "id", "userOwnerId");
			todoListService.updateTodoList(currentTodoList);
			responseEntity = new ResponseEntity<>(currentTodoList, HttpStatus.OK);
		}

		return responseEntity;
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTodoList(@AuthenticationPrincipal UserPrincipal currentUser,
											   @PathVariable("id") TodoList todoList) {
		ResponseEntity<Void> responseEntity;

		if (todoList == null){
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else if (!todoList.getUserOwnerId().equals(currentUser.getId())){
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			todoListService.deleteTodoList(todoList);
			responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return responseEntity;
	}
	
}
