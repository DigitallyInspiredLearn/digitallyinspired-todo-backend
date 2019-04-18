package com.list.todo.services;

import com.list.todo.entity.*;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import com.list.todo.util.ObjectsProvider;
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
    private TagTaskKeyService tagTaskKeyService;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private TodoListService todoListService;

    private static final Long USER_ID = 1L;
    private static final String USER_USERNAME = "username";
    private static final Long SECOND_USER_ID = 2L;
    private static final String SECOND_USER_USERNAME = "username2";
    private static final Long TODO_LIST_ID = 3L;
    private static final String TODO_LIST_NAME = "todoListName";
    private static final String TODO_LIST_COMMENT = "comment";
    private static final Long TODO_LIST_ID_2 = 4L;
    private static final String TODO_LIST_NAME_2 = "todoListName2";
    private static final Long TAG_ID = 5L;
    private static final Long TAG_ID_2 = 6L;

    @Test
    public void getTodoListById_GetExistentTodoList_OptionalOfTodoList() {
        // arrange
        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setTodoListName(TODO_LIST_NAME);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        // act
        Optional<TodoList> returnedTodoList = todoListService.getTodoListById(TODO_LIST_ID);

        // assert
        Assert.assertEquals(returnedTodoList, Optional.of(todoList));
    }

    @Test
    public void getTodoListsByUser_Successful_IterableOfTodoLists() {
        //arrange
        TodoList todoList1 = new TodoList();
        todoList1.setTodoListName(TODO_LIST_NAME);
        TodoList todoList2 = new TodoList();
        todoList2.setTodoListName(TODO_LIST_NAME_2);
        List<TodoList> todoLists = new ArrayList<>();
        todoLists.add(todoList1);
        todoLists.add(todoList2);
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedBy(USER_USERNAME, pageable)).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(null, TodoListStatus.ACTIVE, pageable, null);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
    }

    @Test
    public void addTodoList_Successful_OptionalOfAddedTodoList() {
        //arrange
        TodoList todoList = new TodoList();
        todoList.setTodoListName(TODO_LIST_NAME);
        todoList.setTodoListStatus(TodoListStatus.ACTIVE);
        
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        //act
        Optional<TodoList> addedTodoList = todoListService.addTodoList(
                new TodoListInput(todoList.getTodoListName(), TODO_LIST_COMMENT, new LinkedHashSet<>()), USER_ID);

        //assert
        verify(userService).getUserById(USER_ID);
        Assert.assertEquals(addedTodoList, Optional.of(todoList));
    }

    @Test
    public void updateTodoList_Successful_OptionalOfUpdatedTodoList() {
        //arrange
        String newTodoListName = TODO_LIST_NAME_2;
        TodoList todoList = Mockito.mock(TodoList.class);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        //act
        todoListService.updateTodoList(
                TODO_LIST_ID, new TodoListInput(newTodoListName, TODO_LIST_COMMENT, new LinkedHashSet<>()), USER_ID);

        //assert
        verify(todoList).setTodoListName(newTodoListName);
    }

    @Test
    public void getTodoListsByUser_getAllTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        String userName = USER_USERNAME;
        List<TodoList> todoLists = ObjectsProvider.createListOfTodoLists();
        List<Task> tasks = ObjectsProvider.createListOfTasks();
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(userName);
        List<Long> tagIds = new ArrayList<Long>() {{
            add(TAG_ID);
            add(TAG_ID_2);
        }};

        when(tagTaskKeyService.getTasksByTags(tagIds, userPrincipal.getId())).thenReturn(ObjectsProvider.createSetOfTasks());
        when(todoListRepository.findDistinctByCreatedByAndTodoListStatusAndTasksIn(userPrincipal.getUsername(), TodoListStatus.ACTIVE, pageable, tasks)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ACTIVE, pageable, tagIds);

        // assert
        verify(tagTaskKeyService).getTasksByTags(tagIds, userPrincipal.getId());
        verify(todoListRepository).findDistinctByCreatedByAndTodoListStatusAndTasksIn(userName, TodoListStatus.ACTIVE, pageable, tasks);
        Assert.assertEquals(returnedTodoLists, page);
    }

    @Test
    public void getTodoListsByUser_getActiveTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        List<TodoList> todoLists = ObjectsProvider.createListOfTodoLists();
        todoLists.forEach(todoList -> todoList.setCreatedBy(USER_USERNAME));
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository.findByCreatedByAndTodoListStatus(USER_USERNAME, TodoListStatus.ACTIVE, pageable)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(null, TodoListStatus.ACTIVE, pageable, null);

        // assert
        verify(todoListRepository).findByCreatedByAndTodoListStatus(USER_USERNAME, TodoListStatus.ACTIVE, pageable);
        Assert.assertEquals(returnedTodoLists, page);
    }

    @Test
    public void changeTodoListStatus_OnExistentTodoList_TodoList() {
        // arrange
        TodoList todoList = Mockito.mock(TodoList.class);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(todoListRepository.save(todoList)).thenReturn(todoList);

        // act
        todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);

        // assert
        verify(todoList).setTodoListStatus(TodoListStatus.INACTIVE);
        verify(todoListRepository).save(todoList);
    }

    @Test
    public void deleteTodoList_Successful() {
        //arrange
        TodoList todoList = new TodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        //act
        todoListService.deleteTodoList(TODO_LIST_ID, USER_ID);

        //assert
        verify(shareService).isSharedTodoList(TODO_LIST_ID);
        verify(shareService, never()).deleteShareBySharedTodoListId(TODO_LIST_ID);
        verify(todoListRepository).deleteById(TODO_LIST_ID);
    }

    @Test
    public void deleteSharedTodoList_Successful() {
        //arrange
        TodoList todoList = new TodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(shareService.isSharedTodoList(TODO_LIST_ID)).thenReturn(true);

        //act
        todoListService.deleteTodoList(TODO_LIST_ID, USER_ID);

        //assert
        verify(shareService).isSharedTodoList(TODO_LIST_ID);
        verify(shareService).deleteShareBySharedTodoListId(TODO_LIST_ID);
        verify(todoListRepository, never()).deleteById(TODO_LIST_ID);
    }

    @Test
    public void shareTodoList_Successful() {
        //arrange
        User targetUser = new User();
        TodoList todoList = new TodoList();
        todoList.setTodoListName(TODO_LIST_NAME);

        when(todoListRepository.findById(TODO_LIST_ID_2)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(targetUser));

        //act
        todoListService.shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
        Share share = new Share(targetUser.getId(), todoList);

        //assert
        verify(todoListRepository).findById(SECOND_USER_ID);
        verify(shareService).addShare(share);
    }

    @Test
    public void changeTodoListStatus_OnNonExistentTodoList_Null() {
        // arrange
        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.empty());

        // act
        Optional<TodoList> movedToCartTodoList = todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);

        // assert
        Assert.assertEquals(Optional.empty(), movedToCartTodoList);
        verify(todoListRepository, times(1)).findById(TODO_LIST_ID);
        verify(todoListRepository, never()).save(any(TodoList.class));

    }


}