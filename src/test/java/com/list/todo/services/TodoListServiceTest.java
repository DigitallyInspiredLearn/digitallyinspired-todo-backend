package com.list.todo.services;

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
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoListServiceTest {

    @Mock
    private TodoListRepository todoListRepository;

    @InjectMocks
    private TodoListService todoListService;

    @Mock
    private Pageable pageable;

    @Test
    public void getTodoListsByUser_getTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        String userName = "Vasiliy";
        int countOfTodoLists = 2;
        List<TodoList> todoLists = this.createListOfTodoLists(countOfTodoLists);
        todoLists.forEach(todoList -> todoList.setCreatedBy(userName));
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedByAndTodoListStatus(userName, TodoListStatus.Active, pageable)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userName, TodoListStatus.Active, pageable);

        // assert
        verify(todoListRepository).findByCreatedByAndTodoListStatus(userName, TodoListStatus.Active, pageable);
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
        todoListService.changeTodoListStatus(todoListId, TodoListStatus.Deleted);

        // assert
        verify(todoList).setTodoListStatus(TodoListStatus.Deleted);
        verify(todoListRepository).save(todoList);
    }

    @Test
    public void changeTodoListStatus_OnNonExistentTodoList_Null() {
        // arrange
        Long todoListId = 1L;

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.empty());

        // act
        Optional<TodoList> movedToCartTodoList = todoListService.changeTodoListStatus(todoListId, TodoListStatus.Deleted);

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