package com.list.todo.controllers;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TodoListService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoListControllerTest {

    @Mock
    private TodoListService todoListService;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private TodoListController todoListController;

    @Test
    public void getTodoLists_GetTodoListsByExistentUser_OK() {

        // arrange
        String userName = "Vasiliy";
        List<TodoList> todoLists = this.createListOfTodoLists(3);
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        when(todoListService.getTodoListsByUser(userName, TodoListStatus.ACTIVE, pageable)).thenReturn(todoLists);

        // act
        ResponseEntity<Iterable<TodoList>> response = todoListController.getTodoLists(currentUser, pageable, TodoListStatus.ACTIVE);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(todoLists, response.getBody());
        verify(todoListService).getTodoListsByUser(userName, TodoListStatus.ACTIVE, pageable);
      
    }

    @Test
    public void disableTodoList_OnExistentTodolist_OK() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.ACTIVE)
                .build();

        TodoList movedTodoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.INACTIVE)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.INACTIVE)).thenReturn(Optional.of(movedTodoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.disableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(Optional.of(movedTodoList), response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService).changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

    }

    @Test
    public void disableTodoList_OnNonExistentTodolist_NotFound() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.disableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

    }

    @Test
    public void disableTodoList_disableTodoListOfAnotherUser_Forbidden() {

        // arrange
        String userName = "Vasiliy";
        String userName2 = "Andrey";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName2)
                .todoListStatus(TodoListStatus.ACTIVE)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.disableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

    }

    @Test
    public void enableTodoList_OnExistentTodolist_OK() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.INACTIVE)
                .build();

        TodoList restoredTodoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.ACTIVE)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.ACTIVE)).thenReturn(Optional.of(restoredTodoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.enableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(Optional.of(restoredTodoList), response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService).changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);

    }

    @Test
    public void enableTodoList_OnNonExistentTodolist_NotFound() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.enableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);

    }

    @Test
    public void enableTodoList_restoreTodoListOfAnotherUser_Forbidden() {

        // arrange
        String userName = "Vasiliy";
        String userName2 = "Andrey";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName2)
                .todoListStatus(TodoListStatus.INACTIVE)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.enableTodoList(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);

    }

    private List<TodoList> createListOfTodoLists(int countOfTodoLists) {
        List<TodoList> todoLists = new ArrayList<>(countOfTodoLists);

        for (long i = 0; i < countOfTodoLists; i++) {
            TodoList todoList = new TodoList();
            todoList.setTodoListName("name1");
            todoList.setCreatedBy("Vasiliy");
            todoList.setId(i);

            todoLists.add(todoList);
        }

        return todoLists;
    }
}