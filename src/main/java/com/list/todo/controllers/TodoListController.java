package com.list.todo.controllers;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/todolists")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TodoListController {

	private final TodoListService todoListService;
	private final UserService userService;
	private final ShareService shareService;
	
	@GetMapping("/my")
	public ResponseEntity<List<TodoList>> getTodoListsByUser(@AuthenticationPrincipal UserPrincipal currentUser) {
		
		List<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser.getId());
		return new ResponseEntity<List<TodoList>>(myTodoLists, HttpStatus.OK);
	}
	
	@GetMapping ("/shared")
	public ResponseEntity<List<TodoList>> getSharedTodolists(@AuthenticationPrincipal UserPrincipal currentUser) {

		List<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());
		return new ResponseEntity<List<TodoList>>(sharedTodoLists, HttpStatus.OK);
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
	
	@PostMapping("/{todoListId}/share")
	public ResponseEntity<Void> shareTodoListToUser(@AuthenticationPrincipal UserPrincipal currentUser,
														@RequestParam("username") String username,
														@PathVariable("todoListId") TodoList todoList) {
		ResponseEntity<Void> responseEntity;
		User user = userService.getUserByUsername(username);
		
		if(user == null || todoList == null) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}  else if (!todoList.getUserOwnerId().equals(currentUser.getId()) || 
				currentUser.getUsername().equals(username)) {
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			Share share = new Share();
			share.setSharedUserId(user.getId());
			share.setSharedTodoList(todoList);
			
			shareService.addShare(share);
			responseEntity = new ResponseEntity<>(HttpStatus.OK);
		}
		return responseEntity;
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
