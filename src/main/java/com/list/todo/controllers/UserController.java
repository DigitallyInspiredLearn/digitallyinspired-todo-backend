package com.list.todo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.CurrentUser;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('ROLE_USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		User user = userService.getUserById(currentUser.getId());

		return new UserSummary(user.getId(), user.getUsername(), user.getName());
    }
	
	@PutMapping("/editProfile")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<User> updateUser(@CurrentUser UserPrincipal currentUser, @RequestBody User user) {
		User currUser = userService.getUserById(currentUser.getId());

		if (currUser == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		currUser.setName(user.getName());
		currUser.setUsername(user.getUsername());
		currUser.setEmail(user.getEmail());
		currUser.setPassword(user.getPassword());

		userService.updateUser(currUser);

		return new ResponseEntity<>(currUser, HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteProfile")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<User> deleteUser(@CurrentUser UserPrincipal currentUser) {

		userService.deleteUser(currentUser.getId());

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
