package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.InputTodoList;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FollowerService {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    private final EmailService emailService;

    public List<User> getFollowersByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollower)
                .collect(Collectors.toList());
    }

    public List<UserSummary> getFollowersUserSummariesByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollowerUserSumm)
                .collect(Collectors.toList());
    }

    public boolean followUser(Long currentUserId, String userNameOfFollowedUser) {
        User currUser = userRepository.findById(currentUserId).orElse(null);
        User followedUser = userRepository.findByUsername(userNameOfFollowedUser).orElse(null);
        boolean isSuccess = false;

        if (followedUser != null) {
            followerRepository.save(new Follower(followedUser.getId(), currUser));
            isSuccess = true;
        }

        return isSuccess;
    }

    public void notifyFollowersAboutAddTodoList(UserPrincipal user, InputTodoList todoList){
        List<User> followers = getFollowersByUserId(user.getId());

        for (User follower : followers){
            String subject = "New todo list!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s create new TodoList: \"%s\". ",
                    follower.getName(),
                    user.getName(),
                    todoList.getTodoListName()
            );

            emailService.sendEmail(follower.getEmail(), subject, message);
        }
    }

    public void notifyFollowersAboutSharingTodoList(UserPrincipal user, TodoList todoList, User sharedUser){
        List<User> followers = getFollowersByUserId(user.getId());

        for (User follower : followers){
            String subject = "Todo list shared!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s shared TodoList: \"%s\" to user %s.",
                    follower.getName(),
                    user.getName(),
                    todoList.getTodoListName(),
                    sharedUser.getName()
            );

            emailService.sendEmail(follower.getEmail(), subject, message);
        }
    }

    public void notifyFollowersAboutUpdatingTodoList(UserPrincipal user, TodoList todoList){
        List<User> followers = getFollowersByUserId(user.getId());

        for (User follower : followers){
            String subject = "Todo list updated!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s updated TodoList: \"%s\".",
                    follower.getName(),
                    user.getName(),
                    todoList.getTodoListName()
            );

            emailService.sendEmail(follower.getEmail(), subject, message);
        }
    }

    public void notifyFollowersAboutDeletingTodoList(UserPrincipal user, TodoList todoList){
        List<User> followers = getFollowersByUserId(user.getId());

        for (User follower : followers){
            String subject = "Todo list deleting!";

            String message = String.format(
                    "Hi, %s!\n" +
                            "User %s deleted TodoList: \"%s\".",
                    follower.getName(),
                    user.getName(),
                    todoList.getTodoListName()
            );

            emailService.sendEmail(follower.getEmail(), subject, message);
        }
    }
}
