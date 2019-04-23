package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static com.list.todo.util.ObjectsProvider.createTodoList;
import static com.list.todo.util.ObjectsProvider.createUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NotificationServiceTest {

    @Mock
    private EmailService emailServiceMock;

    @Mock
    private UserSettingsService userSettingsServiceMock;

    @Mock
    private SimpMessagingTemplate webSocketMock;

    @Mock
    private FollowerService followerServiceMock;

    @InjectMocks
    private NotificationService notificationServiceMock;

    @Test
    public void notifyAboutSharingTodoList_SuccessfulNotify() {
        //arrange
        User ownerUser = createUser(1);
        User targetUser = createUser(2);
        TodoList sharedTodoList = createTodoList(ownerUser.getUsername());
        String channel = "/" + targetUser.getName();

        //act
        notificationServiceMock.notifyAboutSharingTodoList(ownerUser, targetUser, sharedTodoList);

        //assert
        verify(targetUser, times(2)).getName();
        verify(ownerUser, times(1)).getName();
        verify(sharedTodoList, times(1)).getTodoListName();

    }

    @Test
    public void notifyAboutAddingTodoList() {
    }

    @Test
    public void notifyAboutUpdatingTodoList() {
    }

    @Test
    public void notifyAboutDeletingTodoList() {
    }
}