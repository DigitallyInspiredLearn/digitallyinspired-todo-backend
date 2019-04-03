package com.list.todo.controllers;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.entity.User;
import com.list.todo.payload.TodoListInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/todolists")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TodoListController {

    private final TodoListService todoListService;
    private final UserService userService;
    private final ShareService shareService;

    @GetMapping("/my")
    public ResponseEntity<Iterable<TodoList>> getMyTodoLists(@AuthenticationPrincipal UserPrincipal currentUser,
                                                             Pageable pageable) {

        Iterable<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser.getUsername(), TodoListStatus.Active, pageable);

        return new ResponseEntity<>(myTodoLists, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Iterable<TodoList>> searchTodoListByName(@AuthenticationPrincipal UserPrincipal currentUser,
                                                                   @RequestParam("name") String partOfTodoListName,
                                                                   Pageable pageable) {
        Iterable<TodoList> todoLists = todoListService.searchTodoListByName(partOfTodoListName + "%", currentUser.getUsername(), pageable);

        return new ResponseEntity<>(todoLists, HttpStatus.OK);
    }

    @GetMapping("/shared")
    public ResponseEntity<Iterable<TodoList>> getMySharedTodoLists(@AuthenticationPrincipal UserPrincipal currentUser) {

        Iterable<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());

        return new ResponseEntity<>(sharedTodoLists, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Iterable<TodoList>> getMovedToCartTodoLists(@AuthenticationPrincipal UserPrincipal currentUser, Pageable pageable) {

        Iterable<TodoList> movedToCartTodoLists = todoListService.getTodoListsByUser(currentUser.getUsername(), TodoListStatus.Deleted, pageable);

        return new ResponseEntity<>(movedToCartTodoLists, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<TodoList>> getTodoList(@PathVariable("id") Long todoListId,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<TodoList>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            responseEntity = new ResponseEntity<>(todoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PostMapping
    public ResponseEntity<Optional<TodoList>> addTodoList(@RequestBody TodoListInput todoListInput,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        Optional<TodoList> todoList = todoListService.addTodoList(todoListInput, currentUser.getId());

        return new ResponseEntity<>(todoList, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<TodoList>> updateTodoList(@RequestBody TodoListInput todoListInput,
                                                             @AuthenticationPrincipal UserPrincipal currentUser,
                                                             @PathVariable("id") Long todoListId) {
        ResponseEntity<Optional<TodoList>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {

            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Optional<TodoList> updatedtodoList = todoListService.updateTodoList(todoList.get().getId(), todoListInput, currentUser.getId());
            responseEntity = new ResponseEntity<>(updatedtodoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PutMapping("/moveToCart/{id}")
    public ResponseEntity<Optional<TodoList>> moveTodoListToCart(@AuthenticationPrincipal UserPrincipal currentUser,
                                               @PathVariable("id") Long todoListId) {
        ResponseEntity<Optional<TodoList>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Optional<TodoList> movedtodoList = todoListService.moveTodoListToCart(todoList.get().getId());
            responseEntity = new ResponseEntity<>(movedtodoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodoList(@AuthenticationPrincipal UserPrincipal currentUser,
                                               @PathVariable("id") Long todoListId) {
        ResponseEntity<Void> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            todoListService.deleteTodoList(todoList.get().getId(), currentUser.getId());
            responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return responseEntity;
    }


    @PostMapping("/{todoListId}/share")
    public ResponseEntity<Void> shareTodoListToUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                    @RequestParam("username") String targetUserUsername,
                                                    @PathVariable("todoListId") Long sharedTodoListId) {
        ResponseEntity<Void> responseEntity;
        Optional<User> targetUserOfSharedTodoList = userService.getUserByUsername(targetUserUsername);
        Optional<TodoList> sharedTodoList = todoListService.getTodoListById(sharedTodoListId);

        if (!targetUserOfSharedTodoList.isPresent() || !sharedTodoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!sharedTodoList.get().getCreatedBy().equals(currentUser.getUsername()) ||
                currentUser.getUsername().equals(targetUserUsername)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (shareService.isSharedTodoListToUser(sharedTodoList.get(), targetUserOfSharedTodoList.get().getId())) {
            responseEntity = new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            todoListService.shareTodoList(targetUserUsername, sharedTodoList.get().getId(), currentUser.getId());
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        }
        return responseEntity;
    }
}
