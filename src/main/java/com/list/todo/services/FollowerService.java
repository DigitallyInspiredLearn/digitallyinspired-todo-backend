package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FollowerService {

    private FollowerRepository followerRepository;

    private final EmailService emailService;

    public List<User> getFollowersByUserId(Long userId) {
        List<Follower> followers = followerRepository.findByFollowedUserId(userId);
        List<User> followedUsers = new ArrayList<>();
        followers.forEach(follower -> followedUsers.add(follower.getFollower()));

        return followedUsers;
    }

    public List<UserSummary> getFollowersUserSummariesByUserId(Long userId) {
        List<Follower> followers = followerRepository.findByFollowedUserId(userId);
        List<UserSummary> followedUsers = new ArrayList<>();
        followers.forEach(follower -> followedUsers.add(new UserSummary(
                        follower.getFollower().getUsername(),
                        follower.getFollower().getName(),
                        follower.getFollower().getEmail())));

        return followedUsers;
    }

    public void followUser(Follower follower) {
        followerRepository.save(follower);
    }

    public void notifyFollowersAboutAddTodoList(UserPrincipal user, TodoList todoList){
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
