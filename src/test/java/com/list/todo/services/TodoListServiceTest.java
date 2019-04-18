package com.list.todo.services;

import com.list.todo.entity.*;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
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

import java.util.*;

import static com.list.todo.util.ObjectsProvider.*;
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
    private NotificationService notificationService;

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
    private static final Long TAG_ID = 5L;
    private static final Long TAG_ID_2 = 6L;

    @Test
    public void getTodoListById_GetExistentTodoList_OptionalOfTodoList() {
        // arrange
        TodoList todoList = createTodoList();
        todoList.setTodoListName(TODO_LIST_NAME);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        // act
        Optional<TodoList> returnedTodoList = todoListService.getTodoListById(TODO_LIST_ID);

        // assert
        Assert.assertEquals(returnedTodoList, Optional.of(todoList));
        verify(todoListRepository, times(1)).findById(TODO_LIST_ID);
    }

    @Test
    public void getTodoListsByUser_WithNotEmptyTasksListAndTodoListStatusIsActive_IterableOfTodoLists() {
        //arrange
        List<TodoList> todoLists = createListOfTodoLists();
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());
        Set<Task> tasks = createSetOfTasks();
        List<Long> tagIds = new ArrayList<>();
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(USER_USERNAME);

        when(tagTaskKeyService.getTasksByTags(tagIds, USER_ID)).thenReturn(tasks);
        when(todoListRepository.findDistinctByCreatedByAndTodoListStatusAndTasksIn(USER_USERNAME, TodoListStatus.ACTIVE, pageable, new ArrayList<>(tasks))).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ACTIVE, pageable, tagIds);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
        verify(tagTaskKeyService, times(1)).getTasksByTags(tagIds, USER_ID);
        verify(todoListRepository, times(1)).findDistinctByCreatedByAndTodoListStatusAndTasksIn(USER_USERNAME, TodoListStatus.ACTIVE, pageable, new ArrayList<>(tasks));
    }

    @Test
    public void getTodoListsByUser_WithNotEmptyTasksListAndTodoListStatusIsAll_IterableOfTodoLists() {
        //arrange
        List<TodoList> todoLists = createListOfTodoLists();
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());
        Set<Task> tasks = createSetOfTasks();
        List<Long> tagIds = new ArrayList<>();
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(USER_USERNAME);

        when(tagTaskKeyService.getTasksByTags(tagIds, USER_ID)).thenReturn(tasks);
        when(todoListRepository.findDistinctByCreatedByAndTasksIn(USER_USERNAME, pageable, new ArrayList<>(tasks))).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ALL, pageable, tagIds);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
        verify(tagTaskKeyService, times(1)).getTasksByTags(tagIds, USER_ID);
        verify(todoListRepository, times(1)).findDistinctByCreatedByAndTasksIn(USER_USERNAME, pageable, new ArrayList<>(tasks));
    }

    @Test
    public void getTodoListsByUser_WithEmptyTasksListAndTodoListStatusIsAll_IterableOfTodoLists() {
        //arrange
        List<TodoList> todoLists = createListOfTodoLists();
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());
        List<Long> tagIds = new ArrayList<>();
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(USER_USERNAME);

        when(tagTaskKeyService.getTasksByTags(tagIds, USER_ID)).thenReturn(new HashSet<>());
        when(todoListRepository.findByCreatedBy(USER_USERNAME, pageable)).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ALL, pageable, tagIds);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
        verify(tagTaskKeyService, times(1)).getTasksByTags(tagIds, USER_ID);
        verify(todoListRepository, times(1)).findByCreatedBy(USER_USERNAME, pageable);
    }

    @Test
    public void getTodoListsByUser_WithEmptyTasksListAndTodoListStatusIsActive_IterableOfTodoLists() {
        //arrange
        List<TodoList> todoLists = createListOfTodoLists();
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());
        List<Long> tagIds = new ArrayList<>();
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(USER_USERNAME);

        when(tagTaskKeyService.getTasksByTags(tagIds, USER_ID)).thenReturn(new HashSet<>());
        when(todoListRepository.findByCreatedByAndTodoListStatus(USER_USERNAME, TodoListStatus.ACTIVE, pageable)).thenReturn(todoListPage);

        //act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ACTIVE, pageable, tagIds);

        //assert
        Assert.assertEquals(todoListPage, returnedTodoLists);
        verify(tagTaskKeyService, times(1)).getTasksByTags(tagIds, USER_ID);
        verify(todoListRepository, times(1)).findByCreatedByAndTodoListStatus(USER_USERNAME, TodoListStatus.ACTIVE, pageable);
    }


    @Test
    public void addTodoList_Successful_OptionalOfAddedTodoList() {
        //arrange
        User user = new User();
        TodoList todoList = createTodoList();
        Set<Task> tasks = createSetOfTasks();
        todoList.setTasks(tasks);

        when(todoListRepository.save(any(TodoList.class))).thenReturn(todoList);
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

        //act
        Optional<TodoList> addedTodoList = todoListService.addTodoList(
                new TodoListInput(todoList.getTodoListName(), TODO_LIST_COMMENT, tasks), USER_ID);

        //assert
        verify(userService, times(1)).getUserById(USER_ID);
        verify(todoListRepository, times(2)).save(any(TodoList.class));
        verify(notificationService, times(1)).notifyAboutAddingTodoList(eq(user), any(TodoList.class));
        Assert.assertEquals(addedTodoList, Optional.of(todoList));
    }

    @Test
    public void updateTodoList_Successful_OptionalOfUpdatedTodoList() {
        //arrange
        User user = new User();
        Set<Task> tasks = createSetOfTasks();
        TodoList todoList = spy(createTodoList());

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(todoListRepository.save(todoList)).thenReturn(todoList);
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

        //act
        todoListService.updateTodoList(
                TODO_LIST_ID, new TodoListInput(TODO_LIST_NAME, TODO_LIST_COMMENT, tasks), USER_ID);

        //assert
        verify(todoList, times(1)).setTodoListName(TODO_LIST_NAME);
        verify(todoList, times(1)).setComment(TODO_LIST_COMMENT);
        verify(todoListRepository, times(1)).save(todoList);
        verify(notificationService, times(1)).notifyAboutUpdatingTodoList(eq(user), any(TodoList.class));
    }

    @Test
    public void getTodoListsByUser_getAllTodoListsByExistentUser_ListOfTodoListsByUser() {
        // arrange
        String userName = USER_USERNAME;
        List<TodoList> todoLists = createListOfTodoLists();
        List<Task> tasks = createListOfTasks();
        Page<TodoList> page = new PageImpl<>(todoLists, pageable, todoLists.size());
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(USER_ID);
        userPrincipal.setUsername(userName);
        List<Long> tagIds = new ArrayList<Long>() {{
            add(TAG_ID);
            add(TAG_ID_2);
        }};

        when(tagTaskKeyService.getTasksByTags(tagIds, userPrincipal.getId())).thenReturn(createSetOfTasks());
        when(todoListRepository.findDistinctByCreatedByAndTodoListStatusAndTasksIn(userPrincipal.getUsername(), TodoListStatus.ACTIVE, pageable, tasks)).thenReturn(page);

        // act
        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userPrincipal, TodoListStatus.ACTIVE, pageable, tagIds);

        // assert
        verify(tagTaskKeyService, times(1)).getTasksByTags(tagIds, userPrincipal.getId());
        verify(todoListRepository, times(1)).findDistinctByCreatedByAndTodoListStatusAndTasksIn(userName, TodoListStatus.ACTIVE, pageable, tasks);
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
        verify(todoList, times(1)).setTodoListStatus(TodoListStatus.INACTIVE);
        verify(todoListRepository, times(1)).save(todoList);
    }

    @Test
    public void deleteTodoList_DeleteNotSharedTodoList_Void() {
        //arrange
        TodoList todoList = new TodoList();
        todoList.setId(TODO_LIST_ID);

        User user = new User();

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(shareService.isSharedTodoList(TODO_LIST_ID)).thenReturn(false);
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

        //act
        todoListService.deleteTodoList(TODO_LIST_ID, USER_ID);

        //assert
        verify(shareService, times(1)).isSharedTodoList(TODO_LIST_ID);
        verify(shareService, never()).deleteShareBySharedTodoListId(TODO_LIST_ID);
        verify(todoListRepository, times(1)).deleteById(TODO_LIST_ID);
        verify(notificationService, times(1)).notifyAboutDeletingTodoList(user, todoList);
    }

    @Test
    public void deleteTodoList_DeleteSharedTodoList_Void() {
        //arrange
        TodoList todoList = new TodoList();
        todoList.setId(TODO_LIST_ID);

        User user = new User();

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(shareService.isSharedTodoList(TODO_LIST_ID)).thenReturn(true);

        //act
        todoListService.deleteTodoList(TODO_LIST_ID, USER_ID);

        //assert
        verify(shareService).isSharedTodoList(TODO_LIST_ID);
        verify(shareService).deleteShareBySharedTodoListId(TODO_LIST_ID);
        verify(todoListRepository, never()).deleteById(TODO_LIST_ID);
        verify(notificationService, never()).notifyAboutDeletingTodoList(user, todoList);
    }

    @Test
    public void shareTodoList_ShareExistentTodoListToExistentUser_ApiResponseWithSuccess() {
        //arrange
        ApiResponse apiResponse = new ApiResponse(true, "You shared your todoList to " + SECOND_USER_USERNAME + "!");
        User user = new User();
        User targetUser = new User();
        targetUser.setId(SECOND_USER_ID);
        TodoList todoList = new TodoList();
        todoList.setTodoListName(TODO_LIST_NAME);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(targetUser));
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

        //act
        ApiResponse result = todoListService.shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
        Share share = new Share(targetUser.getId(), todoList);

        //assert
        Assert.assertEquals(apiResponse, result);
        verify(todoListRepository, times(1)).findById(TODO_LIST_ID);
        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
        verify(shareService, times(1)).addShare(share);
        verify(userService, times(1)).getUserById(USER_ID);
        verify(notificationService, times(1)).notifyAboutSharingTodoList(user, targetUser, todoList);
    }

    @Test
    public void shareTodoList_ShareNonExistentTodoListToExistentUser_ApiResponseWithFailure() {
        //arrange
        ApiResponse apiResponse = new ApiResponse(false, "Something went wrong!");
        User user = new User();
        User targetUser = new User();
        targetUser.setId(SECOND_USER_ID);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.empty());
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(targetUser));

        //act
        ApiResponse result = todoListService.shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);

        //assert
        Assert.assertEquals(apiResponse, result);
        verify(todoListRepository, times(1)).findById(TODO_LIST_ID);
        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
        verify(shareService, never()).addShare(any(Share.class));
        verify(userService, never()).getUserById(USER_ID);
        verify(notificationService, never()).notifyAboutSharingTodoList(eq(user), eq(targetUser), any(TodoList.class));
    }

    @Test
    public void shareTodoList_ShareExistentTodoListToNonExistentUser_ApiResponseWithFailure() {
        //arrange
        ApiResponse apiResponse = new ApiResponse(false, "Something went wrong!");
        User user = new User();
        TodoList todoList = new TodoList();
        todoList.setTodoListName(TODO_LIST_NAME);

        when(todoListRepository.findById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.empty());

        //act
        ApiResponse result = todoListService.shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);

        //assert
        Assert.assertEquals(apiResponse, result);
        verify(todoListRepository, times(1)).findById(TODO_LIST_ID);
        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
        verify(shareService, never()).addShare(any(Share.class));
        verify(userService, never()).getUserById(USER_ID);
        verify(notificationService, never()).notifyAboutSharingTodoList(eq(user), any(User.class), eq(todoList));
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

    @Test
    public void searchTodoListByName_GetExistentTodoLists_IterableOfTodoLists() {
        // arrange
        List<TodoList> todoLists = createListOfTodoLists();
        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());

        when(todoListRepository
                .findByTodoListNameLikeAndCreatedByEqualsAndTodoListStatus(TODO_LIST_NAME + "%",
                        USER_USERNAME, TodoListStatus.ACTIVE, pageable)).thenReturn(todoListPage);

        // act
        Iterable<TodoList> result = todoListService.searchTodoListByName(TODO_LIST_NAME + "%", USER_USERNAME, pageable);

        // assert
        Assert.assertEquals(todoListPage, result);
        verify(todoListRepository, times(1)).findByTodoListNameLikeAndCreatedByEqualsAndTodoListStatus(TODO_LIST_NAME + "%", USER_USERNAME, TodoListStatus.ACTIVE, pageable);
    }
}