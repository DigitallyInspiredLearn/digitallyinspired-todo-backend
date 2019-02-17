package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.TodoListInput;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

}
