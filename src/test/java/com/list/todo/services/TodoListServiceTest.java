package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.User;
import com.list.todo.payload.TodoListInput;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;

import com.list.todo.repositories.TodoListRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoListServiceTest {

    @Mock
    private TodoListRepository todoListRepository;

    @Mock
    private ShareService shareService;

    @Mock
    private UserService userService;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private TodoListService todoListService;

    @Test
    public void getTodoListById_Successful_ReturnsOptionalOfTodolist() {
        // arrange
        long todoListId = 1;
        TodoList todoList = new TodoList();
        todoList.setTodoListName("name");
        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        Optional<TodoList> returnedTodoList = todoListService.getTodoListById(todoListId);

        // assert
        Assert.assertEquals(returnedTodoList, Optional.of(todoList));
    }

    @Test
    public void getTodoListsByUser_Successful_ReturnsIterableOfTodolists() {
        //arrange
        String username = "username";
        TodoList todoList1 = new TodoList();
        todoList1.setTodoListName("name1");
        TodoList todoList2 = new TodoList();
        todoList2.setTodoListName("name2");
        List<TodoList> todoLists = new ArrayList<>();
        todoLists.add(todoList1);
        todoLists.add(todoList2);
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedBy(username, pageable)).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(null, TodoListStatus.ACTIVE, pageable, null);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
    }

    @Test
    public void addTodoList_Successful_ReturnsOptionalOfNewTodolist() {
        //arrange
        long userId = 1;
        TodoList todoList = new TodoList();
        todoList.setTodoListName("todoListName");
        todoList.setTodoListStatus(TodoListStatus.ACTIVE);
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        //act
        Optional<TodoList> addedTodoList = todoListService.addTodoList(
                new TodoListInput(todoList.getTodoListName(), "comment", new LinkedHashSet<>()), userId);

        //assert
        verify(userService).getUserById(userId);
        Assert.assertEquals(addedTodoList, Optional.of(todoList));
    }

    @Test
    public void updateTodoList_Successful_ReturnsOptionalOfUpdatedTodolist() {
        //arrange
        long todoListId = 1;
        long userId = 1;
        String newTodoListName = "updatedName";
        TodoList todoList = Mockito.mock(TodoList.class);

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));

        //act
        todoListService.updateTodoList(
                todoListId, new TodoListInput(newTodoListName, "comment", new LinkedHashSet<>()), userId);

        //assert
        verify(todoList).setTodoListName(newTodoListName);
    }

    @Test
    public void getTodoListsByUser_getAllTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        String userName = "Vasiliy";
        int countOfTodoLists = 2;
        List<TodoList> todoLists = this.createListOfTodoLists(countOfTodoLists);
        todoLists.forEach(todoList -> todoList.setCreatedBy(userName));
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedBy(userName, pageable)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(null, TodoListStatus.ALL, pageable, null);

        // assert
        verify(todoListRepository).findByCreatedBy(userName, pageable);
        Assert.assertEquals(returnedTodoLists, page);
    }

    @Test
    public void getTodoListsByUser_getActiveTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        String userName = "Vasiliy";
        int countOfTodoLists = 2;
        List<TodoList> todoLists = this.createListOfTodoLists(countOfTodoLists);
        todoLists.forEach(todoList -> todoList.setCreatedBy(userName));
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedByAndTodoListStatus(userName, TodoListStatus.ACTIVE, pageable)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(null, TodoListStatus.ACTIVE, pageable, null);

        // assert
        verify(todoListRepository).findByCreatedByAndTodoListStatus(userName, TodoListStatus.ACTIVE, pageable);
        Assert.assertEquals(returnedTodoLists, page);
    }

    @Test
    public void changeTodoListStatus_OnExistentTodoList_TodoList() {
        // arrange
        Long todoListId = 1L;

        TodoList todoList = Mockito.mock(TodoList.class);

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        // act
        todoListService.changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

        // assert
        verify(todoList).setTodoListStatus(TodoListStatus.INACTIVE);
        verify(todoListRepository).save(todoList);
    }

    @Test
    public void deleteTodoList_Successful() {
        //arrange
        long todoListId = 1;
        long userId = 1;
        TodoList todoList = new TodoList();
        todoList.setId(todoListId);

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));

        //act
        todoListService.deleteTodoList(todoListId, userId);

        //assert
        verify(shareService).isSharedTodoList(todoListId);
        verify(shareService, times(0)).deleteShareBySharedTodoListId(todoListId);
        verify(todoListRepository).deleteById(todoListId);
    }

    @Test
    public void deleteSharedTodoList_Successful() {
        //arrange
        long todoListId = 1;
        long userId = 1;
        TodoList todoList = new TodoList();
        todoList.setId(todoListId);

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));
        when(shareService.isSharedTodoList(todoListId)).thenReturn(true);

        //act
        todoListService.deleteTodoList(todoListId, userId);

        //assert
        verify(shareService).isSharedTodoList(todoListId);
        verify(shareService).deleteShareBySharedTodoListId(todoListId);
        verify(todoListRepository, times(0)).deleteById(todoListId);
    }

    @Test
    public void shareTodoList_Successful() {
        //arrange
        String targetUserUsername = "vitaliy";
        User targetUser = new User();
        long ownerUserId = 1;
        long sharedTodoListId = 1;
        TodoList todoList = new TodoList();
        todoList.setTodoListName("name");

        when(todoListRepository.findById(sharedTodoListId)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(targetUserUsername)).thenReturn(Optional.of(targetUser));

        //act
        todoListService.shareTodoList(targetUserUsername, sharedTodoListId, ownerUserId);
        Share share = new Share(targetUser.getId(), todoList);

        //assert
        verify(todoListRepository).findById(sharedTodoListId);
        verify(shareService).addShare(share);
    }
      
    public void changeTodoListStatus_OnNonExistentTodoList_Null() {
        // arrange
        Long todoListId = 1L;

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.empty());

        // act
        Optional<TodoList> movedToCartTodoList = todoListService.changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

        // assert
        Assert.assertEquals(Optional.empty(), movedToCartTodoList);
        verify(todoListRepository, times(1)).findById(todoListId);
        verify(todoListRepository, never()).save(any(TodoList.class));

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