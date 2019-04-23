package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import com.list.todo.util.ObjectsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void notifyAboutSharingTodoList_IfEnableEmailNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(true)
                .isEnableWebSocketNotification(false)
                .build();

        User ownerUser = createUserWithUserSettings(1, userSettings);
        User targetUser = createUserWithUserSettings(2, userSettings);
        TodoList sharedTodoList = createTodoList(ownerUser.getUsername());

        when(userSettingsServiceMock.getUserSettingsByUserId(targetUser.getId())).thenReturn(Optional.of(targetUser.getUserSettings()));

        //act
        notificationServiceMock.notifyAboutSharingTodoList(ownerUser, targetUser, sharedTodoList);

        //assert
        verify(emailServiceMock, times(1)).sendEmail(eq(targetUser.getEmail()), anyString(), anyString());
        verify(webSocketMock, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    public void notifyAboutSharingTodoList_IfEnableWebSocketNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(false)
                .isEnableWebSocketNotification(true)
                .build();

        User ownerUser = createUserWithUserSettings(1, userSettings);
        User targetUser = createUserWithUserSettings(2, userSettings);
        TodoList sharedTodoList = createTodoList(ownerUser.getUsername());

        when(userSettingsServiceMock.getUserSettingsByUserId(targetUser.getId())).thenReturn(Optional.of(targetUser.getUserSettings()));

        //act
        notificationServiceMock.notifyAboutSharingTodoList(ownerUser, targetUser, sharedTodoList);

        //assert
        verify(emailServiceMock, never()).sendEmail(eq(targetUser.getEmail()), anyString(), anyString());
        verify(webSocketMock, times(1)).convertAndSend(anyString(), anyString());
    }

    @Test
    public void notifyAboutAddingTodoList_IfEnableEmailNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(true)
                .isEnableWebSocketNotification(false)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutAddingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            verify(emailServiceMock, times(1)).sendEmail(eq(follower.getEmail()), anyString(), anyString());
            verify(webSocketMock, never()).convertAndSend(anyString(), anyString());
        });
    }

    @Test
    public void notifyAboutAddingTodoList_IfEnableWebSocketNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(false)
                .isEnableWebSocketNotification(true)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutAddingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            String channel = "/" + follower.getName();
            verify(emailServiceMock, never()).sendEmail(anyString(), anyString(), anyString());
            verify(webSocketMock, times(1)).convertAndSend(eq(channel), anyString());
        });

    }

    @Test
    public void notifyAboutUpdatingTodoList_IfEnableEmailNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(true)
                .isEnableWebSocketNotification(false)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutUpdatingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            verify(emailServiceMock, times(1)).sendEmail(eq(follower.getEmail()), anyString(), anyString());
            verify(webSocketMock, never()).convertAndSend(anyString(), anyString());
        });
    }

    @Test
    public void notifyAboutUpdatingTodoList_IfEnableWebSocketNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(false)
                .isEnableWebSocketNotification(true)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutUpdatingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            String channel = "/" + follower.getName();
            verify(emailServiceMock, never()).sendEmail(anyString(), anyString(), anyString());
            verify(webSocketMock, times(1)).convertAndSend(eq(channel), anyString());
        });
    }

    @Test
    public void notifyAboutDeletingTodoList_IfEnableEmailNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(true)
                .isEnableWebSocketNotification(false)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutDeletingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            verify(emailServiceMock, times(1)).sendEmail(eq(follower.getEmail()), anyString(), anyString());
            verify(webSocketMock, never()).convertAndSend(anyString(), anyString());
        });
    }

    @Test
    public void notifyAboutDeletingTodoList_IfEnableWebSocketNotification_SuccessfulNotify() {
        //arrange
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(false)
                .isEnableWebSocketNotification(true)
                .build();

        List<User> followers = ObjectsProvider.createListOfFollowers(userSettings);

        User ownerUser = createUserWithUserSettings(3, userSettings);
        TodoList addedTodoList = createTodoList(ownerUser.getUsername());

        followers.forEach(follower -> when(userSettingsServiceMock
                .getUserSettingsByUserId(follower.getId())).thenReturn(Optional.of(follower.getUserSettings())));
        when(followerServiceMock.getFollowersByUserId(ownerUser.getId())).thenReturn(followers);

        //act
        notificationServiceMock.notifyAboutDeletingTodoList(ownerUser, addedTodoList);

        //assert
        followers.forEach(follower -> {
            String channel = "/" + follower.getName();
            verify(emailServiceMock, never()).sendEmail(anyString(), anyString(), anyString());
            verify(webSocketMock, times(1)).convertAndSend(eq(channel), anyString());
        });
    }
}