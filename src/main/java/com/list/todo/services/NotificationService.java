package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService implements Notifiable {

    private final EmailService emailService;
    private final UserSettingsService userSettingsService;
    private final SimpMessagingTemplate webSocket;
    private final FollowerService followerService;

    @Override
    public void notifyAboutSharingTodoList(User ownerUser, User targetUser, TodoList sharedTodoList) {

        String link = "http://";
        String subject = "You have a new shared todo list!";
        String channel = "/" + targetUser.getName();

        String message = String.format(
                "Hi, %s!\n" +
                        "User %s shared with you TodoList: \"%s\". " +
                        "Follow the link to view: %s",
                targetUser.getName(),
                ownerUser.getName(),
                sharedTodoList.getTodoListName(),
                link
        );

        sendNotification(targetUser, subject, channel, message);
    }

    @Override
    public void notifyAboutAddingTodoList(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers) {
            String subject = "Todo list was added!";

            String channel = "/" + follower.getName();

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s added TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );

            sendNotification(follower, subject, channel, message);
        }
    }

    @Override
    public void notifyAboutUpdatingTodoList(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers) {
            String subject = "Todo list was updated!";

            String channel = "/" + follower.getName();

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s updated TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );

            sendNotification(follower, subject, channel, message);
        }
    }

    @Override
    public void notifyAboutDeletingTodoList(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers) {
            String subject = "Todo list was deleted!";

            String channel = "/" + follower.getName();

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s deleted TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );

            sendNotification(follower, subject, channel, message);
        }
    }

    private void sendNotification(User targetUser, String subject, String channel, String message) {
        userSettingsService.getUserSettingsByUserId(targetUser.getId())
                .ifPresent(userSettings -> {
                    if (userSettings.getIsEnableEmailNotification()) {
                        emailService.sendEmail(targetUser.getEmail(), subject, message);
                    }
                });

        userSettingsService.getUserSettingsByUserId(targetUser.getId())
                .ifPresent(userSettings -> {
                    if (userSettings.getIsEnableWebSocketNotification()) {
                        webSocket.convertAndSend(channel, message);
                    }
                });
    }
}