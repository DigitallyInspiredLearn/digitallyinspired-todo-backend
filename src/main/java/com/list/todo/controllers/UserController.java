package com.list.todo.controllers;

import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class UserController {
	
	private final UserService userService;

	@GetMapping("/me")
    public UserSummary getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
		User user = userService.getUserById(currentUser.getId());

		return new UserSummary(user.getId(), user.getUsername(), user.getName());
    }
	
	@PutMapping("/editProfile")
	public ResponseEntity<User> updateUser(@AuthenticationPrincipal UserPrincipal currentUser,
										   @RequestBody User user) {
		User currUser = userService.getUserById(currentUser.getId());
		ResponseEntity<User> responseEntity;

		if (currUser == null){
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			BeanUtils.copyProperties(user, currUser, "id", "roles");
			userService.updateUser(currUser);
			responseEntity = new ResponseEntity<>(currUser, HttpStatus.OK);
		}
		return responseEntity;
	}
	
	@DeleteMapping("/deleteProfile")
	public ResponseEntity<User> deleteUser(@AuthenticationPrincipal UserPrincipal currentUser) {

		userService.deleteUser(currentUser.getId());

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
