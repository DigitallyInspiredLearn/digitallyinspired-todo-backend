package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.repositories.TodoListRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private ShareService shareService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoListService todoListService;

    @Mock
    private Pageable pageable;

    @Test
    public void getTodoListsByUser_getTodoListsByExistentUser_ReturnsTodoListsByUser() {
        // arrange
        String userName = "Vasiliy";
        List<TodoList> todoLists = this.createListOfTodoLists(2);
        todoLists.forEach(todoList -> todoList.setCreatedBy(userName));
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedByAndTodoListStatus(userName, TodoListStatus.Active, pageable)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userName, TodoListStatus.Active, pageable);

        // assert
        Assert.assertEquals(returnedTodoLists, page);
    }

    @Test
    public void moveTodoListToCart_moveExistentTodoListToCart_ReturnsMovedToCartTodoList() {
        // arrange
        Long todoListId = 1L;

        TodoList todoList = this.createTodoList();
        when(todoListRepository.save(todoList)).thenReturn(todoList);
        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        Optional<TodoList> movedToCartTodoList = todoListService.moveTodoListToCart(1L);

        // assert
        Assert.assertEquals(Optional.of(todoList), movedToCartTodoList);
        verify(todoListRepository).save(todoList);

    }

    @Test
    public void moveTodoListToCart_moveNonExistentTodoListToCart_ReturnsNull() {
        // arrange
        Long todoListId = 1L;

        when(todoListRepository.findById(todoListId)).thenReturn(Optional.empty());

        // act
        Optional<TodoList> movedToCartTodoList = todoListService.moveTodoListToCart(1L);

        // assert
        Assert.assertEquals(Optional.empty(), movedToCartTodoList);
        verify(todoListRepository, times(0)).save(any(TodoList.class));

    }

    /*@Test
    public void getTodoListById() {
        // arrange
        long todoListId = 1;
        String userName = "Vasiliy";
        TodoList todoList = new TodoList();
        todoList.setTodoListName("name");
        todoList.setCreatedBy(userName);
        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));

        // act
        Optional<TodoList> returnedTodoList = todoListService.getTodoListById(todoListId);

        // assert
        Assert.assertEquals(returnedTodoList, Optional.of(todoList));
    }*/


    /*@Test
    public void addTodoList() {
        Long userId = 1L;
        String userName = "Vasiliy";
        TodoList todoList = new TodoList();
        todoList.setTodoListName("name");
        todoList.setCreatedBy(userName);
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        Optional<TodoList> addedTodoList = todoListService.addTodoList(
                new TodoListInput(todoList.getTodoListName(), new LinkedHashSet<>()), userId);

        verify(userService).getUserById(userId);
        Assert.assertEquals(addedTodoList, Optional.of(todoList));
        //verify(notificationService).notifyFollowersAboutAddingTodolist(any(User.class), any(TodoList.class));
    }*/

    /*@Test
    public void updateTodoList() {
        long todoListId = 1;
        long userId = 1;
        TodoList todoList = new TodoList();
        todoList.setTodoListName("updatedName");
        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(new TodoList()));
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        Optional<TodoList> updatedTodoList = todoListService.updateTodoList(
                todoListId, new TodoListInput(todoList.getTodoListName(), new LinkedHashSet<>()), userId);

        Assert.assertEquals(updatedTodoList, Optional.of(todoList));
    }*/

    /*@Test
    public void deleteTodoList() {
        long todoListId = 1;
        long userId = 1;
        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(new TodoList()));

        todoListService.deleteTodoList(todoListId, userId);
        verify(shareService).deleteShareBySharedTodoListId(todoListId);
    }*/

    /*@Test
    public void shareTodoList() {
        String targetUserUsername = "vitaliy";
        User targetUser = new User();
        long ownerUserId = 1;
        String userName = "Vasiliy";
        long sharedTodoListId = 1;
        TodoList todoList = new TodoList();
        todoList.setTodoListName("name");
        todoList.setCreatedBy(userName);
        when(todoListRepository.findById(sharedTodoListId)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(targetUserUsername)).thenReturn(Optional.of(targetUser));

        todoListService.shareTodoList(targetUserUsername, sharedTodoListId, ownerUserId);
        Share share = new Share(targetUser.getId(), todoList);

        verify(todoListRepository).findById(sharedTodoListId);
        verify(shareService).addShare(share);

    }*/

    private TodoList createTodoList() {
        return TodoList.builder()
                .todoListName("todoList")
                .todoListStatus(TodoListStatus.Active)
                .build();
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