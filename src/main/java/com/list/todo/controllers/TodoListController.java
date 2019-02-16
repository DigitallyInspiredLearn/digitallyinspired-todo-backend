package com.list.todo.controllers;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.TodoListInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<Iterable<TodoList>> getMyTodoLists(@AuthenticationPrincipal UserPrincipal currentUser) {

        Iterable<TodoList> myTodoLists = todoListService.getTodoListsByUser(currentUser.getId());

        return new ResponseEntity<>(myTodoLists, HttpStatus.OK);
    }

    @GetMapping("/shared")
    public ResponseEntity<Iterable<TodoList>> getMySharedTodoLists(@AuthenticationPrincipal UserPrincipal currentUser) {

        Iterable<TodoList> sharedTodoLists = shareService.getSharedTodoListsByUser(currentUser.getId());

        return new ResponseEntity<>(sharedTodoLists, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoList> getTodoList(@PathVariable("id") TodoList todoList,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<TodoList> responseEntity;

        // TODO: придумать как убрать условные конструкции
        // Вариант 1: перенести условия в сервисы, а из сервисов возвращать ApiResponse.
        // Тогда статус всегда будет HttpStatus.OK, но в контроллерах не будет лишней логики
        if (todoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.getUserOwnerId().equals(currentUser.getId())) {
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
                                                             @PathVariable("id") TodoList currentTodoList) {
        ResponseEntity<Optional<TodoList>> responseEntity;

        if (currentTodoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Optional<TodoList> todoList = todoListService.updateTodoList(currentTodoList.getId(), todoListInput, currentUser.getId());
            responseEntity = new ResponseEntity<>(todoList, HttpStatus.OK);
        }

        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodoList(@AuthenticationPrincipal UserPrincipal currentUser,
                                               @PathVariable("id") TodoList todoList) {
        ResponseEntity<Void> responseEntity;

        if (todoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!todoList.getUserOwnerId().equals(currentUser.getId())) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            todoListService.deleteTodoList(todoList.getId(), currentUser.getId());
            responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return responseEntity;
    }


    @PostMapping("/{todoListId}/share")
    public ResponseEntity<Void> shareTodoListToUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                    @RequestParam("username") String targetUserUsername,
                                                    @PathVariable("todoListId") TodoList sharedTodoList) {
        ResponseEntity<Void> responseEntity;
        Optional<User> targetUserOfSharedTodoList = userService.getUserByUsername(targetUserUsername);

        if (!targetUserOfSharedTodoList.isPresent() || sharedTodoList == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (!sharedTodoList.getUserOwnerId().equals(currentUser.getId()) ||
                currentUser.getUsername().equals(targetUserUsername)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (shareService.isSharedTodoListToUser(sharedTodoList, targetUserOfSharedTodoList.get())) {
            responseEntity = new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            todoListService.shareTodoList(targetUserUsername, sharedTodoList.getId(), currentUser.getId());
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        }
        return responseEntity;
    }
}
