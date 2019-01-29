package com.list.todo.controllers;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todolists")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TodoListController {

	private final TodoListService todoListService;
	private final UserService userService;
	private final ShareService shareService;
	private final FollowerService followerService;
	
	@GetMapping("/my")
	public ResponseEntity<List<TodoList>> getTodoListsByUser(@AuthenticationPrincipal UserPrincipal currentUser) {
		
		List<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser.getId());

		return new ResponseEntity<>(myTodoLists, HttpStatus.OK);
	}
	
	@GetMapping ("/shared")
	public ResponseEntity<List<TodoList>> getSharedTodoLists(@AuthenticationPrincipal UserPrincipal currentUser) {

		List<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());

		return new ResponseEntity<>(sharedTodoLists, HttpStatus.OK);
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
		followerService.notifyFollowersAboutAddTodoList(currentUser, todoList);

		return new ResponseEntity<>(todoList, HttpStatus.OK);
	}
	
	@PostMapping("/{todoListId}/share")
	public ResponseEntity<Void> shareTodoListToUser(@AuthenticationPrincipal UserPrincipal currentUser,
													@RequestParam("username") String sharedUsername,
													@PathVariable("todoListId") TodoList sharedTodoList) {
		ResponseEntity<Void> responseEntity;

		User sharedUser = userService.getUserByUsername(sharedUsername);
		
		if(sharedUser == null || sharedTodoList == null) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}  else if (!sharedTodoList.getUserOwnerId().equals(currentUser.getId()) || 
				currentUser.getUsername().equals(sharedUsername)) {
			responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			Share share = new Share(sharedUser.getId(), sharedTodoList);
			
			shareService.addShare(share);
			shareService.sendNotificationAboutShareTodoList(sharedUser, currentUser, sharedTodoList);
			followerService.notifyFollowersAboutSharingTodoList(currentUser, sharedTodoList, sharedUser);

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
			followerService.notifyFollowersAboutUpdatingTodoList(currentUser, todoList);

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
			followerService.notifyFollowersAboutDeletingTodoList(currentUser, todoList);

			responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return responseEntity;
	}
		
}
