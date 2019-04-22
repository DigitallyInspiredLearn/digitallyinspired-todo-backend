package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.payload.UserStatistics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserStatisticsServiceTest {

    private static final Long CURRENT_USER_ID = 1L;

    @Mock
    private TodoListService todoListService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private FollowerService followerService;

    @InjectMocks
    private UserStatisticsService userStatisticsService;


    @Test
    public void getUserStatisticsByUserId_OnExistentUser_ReturnsAnObjectOfUserStatistics() {
        //arrange
        User user = createUser(1);
        UserStatistics userStatistics = createUserStatistics();

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(todoListService.countTodoListsByCreatedBy(user.getUsername()))
                .thenReturn(userStatistics.getTodoListsNumber());
        when(taskService.countTasksByCreatedBy(user.getUsername()))
                .thenReturn(userStatistics.getTasksNumber());
        when(taskService.countTasksByCreatedByAndIsComplete(user.getUsername(), true))
                .thenReturn(userStatistics.getCompletedTasksNumber());
        when(followerService.getFollowersUserSummariesByUserId(CURRENT_USER_ID))
                .thenReturn(createListOfUserSummaries(userStatistics.getFollowersNumber()));
        when(followerService.getFollowedUserSummariesByUserId(CURRENT_USER_ID))
                .thenReturn(createListOfUserSummaries(userStatistics.getFollowedUsersNumber()));

        //act
        UserStatistics returnedUserStatistics = userStatisticsService.getUserStatisticsByUserId(CURRENT_USER_ID);

        //assert
        Assert.assertEquals(userStatistics, returnedUserStatistics);
        verify(userService).getUserById(CURRENT_USER_ID);
        verify(todoListService).countTodoListsByCreatedBy(user.getUsername());
        verify(taskService).countTasksByCreatedBy(user.getUsername());
        verify(taskService).countTasksByCreatedByAndIsComplete(user.getUsername(), true);
        verify(followerService).getFollowersUserSummariesByUserId(CURRENT_USER_ID);
        verify(followerService).getFollowedUserSummariesByUserId(CURRENT_USER_ID);
    }

    @Test
    public void getUserStatisticsByUserId_OnNonExistentUser_ReturnsAnObjectOfUserStatistics() {
        //arrange
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(Optional.empty());

        //act
        userStatisticsService.getUserStatisticsByUserId(CURRENT_USER_ID);

        //assert
        verify(userService).getUserById(CURRENT_USER_ID);
        verify(todoListService, times(0)).countTodoListsByCreatedBy(anyString());
        verify(taskService, times(0)).countTasksByCreatedBy(anyString());
        verify(taskService, times(0)).countTasksByCreatedByAndIsComplete(anyString(), eq(true));
        verify(followerService, times(0)).getFollowersUserSummariesByUserId(CURRENT_USER_ID);
        verify(followerService, times(0)).getFollowedUserSummariesByUserId(CURRENT_USER_ID);
    }

}
