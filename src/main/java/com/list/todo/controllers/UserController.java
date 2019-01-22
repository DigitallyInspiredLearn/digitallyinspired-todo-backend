package com.list.todo.controllers;

import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.CurrentUser;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		User user = userService.getUserById(currentUser.getId());
		
        UserSummary userSummary = new UserSummary(user.getId(), user.getUsername(), user.getName(), user.getTodoLists());
        return userSummary;
    }
	
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getAllUsers();
		if (users.isEmpty()){
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUser(@PathVariable Long id) {
		User user = userService.getUserById(id);
		if (user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> addUser(@RequestBody User user, UriComponentsBuilder ucBuilder) {
		if (userService.isUserExist(user)){
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		userService.saveUser(user);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/users/{id}").buildAndExpand(user.getId()).toUri());
		return new ResponseEntity<>(headers, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
		User currentUser = userService.getUserById(id);

		if (currentUser == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		// TODO: доделать обновление пользователя (обработать пустые поля)
		currentUser.setName(user.getName());
		currentUser.setUsername(user.getUsername());
		currentUser.setEmail(user.getEmail());
		currentUser.setPassword(user.getPassword());
		currentUser.setTodoLists(user.getTodoLists());
		currentUser.setRoles(user.getRoles());

		userService.updateUser(currentUser);

		return new ResponseEntity<>(currentUser, HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> deleteUser(@PathVariable Long id) {
		User user = userService.getUserById(id);

		if (user == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		userService.deleteUser(id);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
