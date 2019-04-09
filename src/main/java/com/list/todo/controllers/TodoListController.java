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

import java.util.List;
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
                                                             @RequestBody List<Long> tagsIds,
                                                             Pageable pageable) {

        Iterable<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser, pageable, tagsIds);

        return new ResponseEntity<>(myTodoLists, HttpStatus.OK);
    }

    @GetMapping("/shared")
    public ResponseEntity<Iterable<TodoList>> getMySharedTodoLists(@AuthenticationPrincipal UserPrincipal currentUser) {

        Iterable<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());

        return new ResponseEntity<>(sharedTodoLists, HttpStatus.OK);
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
            Optional<TodoList> updatedTodoList = todoListService.updateTodoList(todoList.get().getId(), todoListInput, currentUser.getId());
            responseEntity = new ResponseEntity<>(updatedTodoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<Optional<TodoList>> disableTodoList(@AuthenticationPrincipal UserPrincipal currentUser,
                                                              @PathVariable("id") Long todoListId) {
        ResponseEntity<Optional<TodoList>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Optional<TodoList> movedTodoList = todoListService.changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);
            responseEntity = new ResponseEntity<>(movedTodoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<Optional<TodoList>> enableTodoList(@AuthenticationPrincipal UserPrincipal currentUser,
                                                             @PathVariable("id") Long todoListId) {
        ResponseEntity<Optional<TodoList>> responseEntity;
        Optional<TodoList> todoList = todoListService.getTodoListById(todoListId);

        if (!todoList.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.get().getCreatedBy().equals(currentUser.getUsername())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Optional<TodoList> restoredTodoList = todoListService.changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);
            responseEntity = new ResponseEntity<>(restoredTodoList, HttpStatus.OK);
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

    @GetMapping("/search")
    public ResponseEntity<Iterable<TodoList>> getTodoListsByName(@AuthenticationPrincipal UserPrincipal currentUser,
                                                                 @RequestParam("name") String partOfTodoListName,
                                                                 Pageable pageable) {
        Iterable<TodoList> todoLists = todoListService.searchTodoListByName(partOfTodoListName + "%", currentUser.getUsername(), pageable);

        return new ResponseEntity<>(todoLists, HttpStatus.OK);
    }
}
