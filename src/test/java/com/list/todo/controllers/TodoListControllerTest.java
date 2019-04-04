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
    public void getMyTodoLists_GetTodoListsByExistentUser_OK() {

        // arrange
        String userName = "Vasiliy";
        List<TodoList> todoLists = this.createListOfTodoLists(3);
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        when(todoListService.getTodoListsByUser(userName, TodoListStatus.Active, pageable)).thenReturn(todoLists);

        // act
        ResponseEntity<Iterable<TodoList>> response = todoListController.getMyTodoLists(currentUser, pageable);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(todoLists, response.getBody());
        verify(todoListService).getTodoListsByUser(userName, TodoListStatus.Active, pageable);
    }

    @Test
    public void getMovedToCartTodoLists_GetTodoListsByExistentUser_OK() {
        // arrange
        String userName = "Vasiliy";
        List<TodoList> todoLists = this.createListOfTodoLists(3);
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        when(todoListService.getTodoListsByUser(userName, TodoListStatus.Deleted, pageable)).thenReturn(todoLists);

        // act
        ResponseEntity<Iterable<TodoList>> response = todoListController.getMovedToCartTodoLists(currentUser, pageable);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(todoLists, response.getBody());
        verify(todoListService).getTodoListsByUser(userName, TodoListStatus.Deleted, pageable);
    }

    @Test
    public void moveTodoListToCart_OnExistentTodolist_OK() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.Active)
                .build();

        TodoList movedTodoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.Deleted)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.Deleted)).thenReturn(Optional.of(movedTodoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.moveTodoListToCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(Optional.of(movedTodoList), response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService).changeTodoListStatus(todoListId, TodoListStatus.Deleted);

    }

    @Test
    public void moveTodoListToCart_OnNonExistentTodolist_NotFound() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.moveTodoListToCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.Deleted);

    }

    @Test
    public void moveTodoListToCart_moveTodoListOfAnotherUser_Forbidden() {

        // arrange
        String userName = "Vasiliy";
        String userName2 = "Andrey";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName2)
                .todoListStatus(TodoListStatus.Active)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.moveTodoListToCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.Deleted);

    }

    @Test
    public void restoreTodoListFromCart_OnExistentTodolist_OK() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.Deleted)
                .build();

        TodoList restoredTodoList = TodoList.builder()
                .createdBy(userName)
                .todoListStatus(TodoListStatus.Active)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.Active)).thenReturn(Optional.of(restoredTodoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.restoreTodoListFromCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(Optional.of(restoredTodoList), response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService).changeTodoListStatus(todoListId, TodoListStatus.Active);

    }

    @Test
    public void restoreTodoListFromCart_OnNonExistentTodolist_NotFound() {

        // arrange
        String userName = "Vasiliy";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.restoreTodoListFromCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.Active);

    }

    @Test
    public void restoreTodoListFromCart_restoreTodoListOfAnotherUser_Forbidden() {

        // arrange
        String userName = "Vasiliy";
        String userName2 = "Andrey";
        UserPrincipal currentUser = new UserPrincipal();
        currentUser.setUsername(userName);

        Long todoListId = 1L;
        TodoList todoList = TodoList.builder()
                .createdBy(userName2)
                .todoListStatus(TodoListStatus.Deleted)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        ResponseEntity<Optional<TodoList>> response = todoListController.restoreTodoListFromCart(currentUser, todoListId);

        // assert
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertNull(response.getBody());
        verify(todoListService).getTodoListById(todoListId);
        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.Active);

    }

    @Test
    public void restoreTodoListFromCart() {
        // arrange
        // act
        // assert
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