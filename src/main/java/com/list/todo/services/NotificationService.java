package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {

    private SimpMessagingTemplate webSocket;
    private EmailService emailService;
    private FollowerService followerService;


    public void notifyAboutSharingTodolist(User ownerUser, User targetUser, TodoList sharedTodoList) {
        String link = "http://localhost:8080/api/todolists/shared";
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

        emailService.sendEmail(targetUser.getEmail(), subject, message);
        webSocket.convertAndSend(channel, message);
    }

    public void notifyFollowersAboutAddingTodolist(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers) {
            String subject = "Todo list was added!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s added TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );
            String channel = "/" + follower.getName();
            emailService.sendEmail(follower.getEmail(), subject, message);
            webSocket.convertAndSend(channel, message);
        }
    }

    public void notifyFollowersAboutUpdatingTodolist(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers){
            String subject = "Todo list was updated!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s updated TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );
            emailService.sendEmail(follower.getEmail(), subject, message);
            String channel = "/" + follower.getName();
            webSocket.convertAndSend(channel, message);
        }
    }

    public void notifyFollowersAboutDeletingTodolist(User ownerUser, TodoList todoList) {
        List<User> followers = followerService.getFollowersByUserId(ownerUser.getId());

        for (User follower : followers){
            String subject = "Todo list was deleted!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s deleted TodoList: \"%s\".",
                    follower.getName(),
                    ownerUser.getName(),
                    todoList.getTodoListName()
            );

            emailService.sendEmail(follower.getEmail(), subject, message);
            String channel = "/" + follower.getName();
            webSocket.convertAndSend(channel, message);
        }
    }
}