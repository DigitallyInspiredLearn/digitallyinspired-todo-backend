package com.list.todo.controllers;

import com.list.todo.entity.Follower;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
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
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class UserController {
	
	private final UserService userService;
	private final TodoListService todoListService;
	private final ShareService shareService;
	private final FollowerService followerService;

	@GetMapping("/me")
    public ResponseEntity<UserSummary> getUserInfo(@AuthenticationPrincipal UserPrincipal currentUser) {
		User user = userService.getUserById(currentUser.getId());
		UserSummary userSummary = new UserSummary(user.getUsername(), user.getName(), user.getEmail());
		
		return new ResponseEntity<UserSummary>(userSummary, HttpStatus.OK);
    }
	
	@GetMapping("/search")
	public ResponseEntity<Set<String>> searchUserNamesByPartOfUserName(@RequestParam("username") String username) {

		return new ResponseEntity<>(userService.searchUsersByPartOfUsername(username), HttpStatus.OK);
	}
	
	@GetMapping("/userStats")
	public ResponseEntity<UserStats> getUserStats(@AuthenticationPrincipal UserPrincipal currentUser) {

		UserStats userStats = new UserStats();
		
		List<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser.getId());
		userStats.setMyTodoLists(myTodoLists);
		
		List<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());
		userStats.setSharedTodoLists(sharedTodoLists);

		return new ResponseEntity<>(userStats, HttpStatus.OK);
	}
	
	@PutMapping("/editProfile")
	public ResponseEntity<User> updateMyProfile(@AuthenticationPrincipal UserPrincipal currentUser,
										   @RequestBody User user) {
		ResponseEntity<User> responseEntity;
		User currUser = userService.getUserById(currentUser.getId());

		if (currUser == null){
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			BeanUtils.copyProperties(user, currUser, "id", "role", "email", "password");
			userService.updateUser(currUser);
			responseEntity = new ResponseEntity<>(currUser, HttpStatus.OK);
		}
		return responseEntity;
	}
	
	@DeleteMapping("/deleteProfile")
	public ResponseEntity<User> deleteMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {

		userService.deleteUser(currentUser.getId());

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/followUser")
	public ResponseEntity<User> followUser(@AuthenticationPrincipal UserPrincipal currentUser,
										   @RequestParam("username") String username) {
		ResponseEntity<User> responseEntity;
		
		User currUser = userService.getUserById(currentUser.getId());
		User followedUser = userService.getUserByUsername(username); 
		
		if (followedUser == null) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			
			Follower follower = new Follower(followedUser.getId(), currUser);
			followerService.followUser(follower);
			responseEntity = new ResponseEntity<>(followedUser, HttpStatus.OK);
		}

		return responseEntity;
	}

	@GetMapping("/followers")
	public ResponseEntity<List<UserSummary>> getFollowers(@AuthenticationPrincipal UserPrincipal currentUser) {

		return new ResponseEntity<>(followerService.getFollowersUserSummariesByUserId(currentUser.getId()), HttpStatus.OK);
	}
}
