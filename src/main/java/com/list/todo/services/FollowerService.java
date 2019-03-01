package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FollowerService {

    private FollowerRepository followerRepository;
    private UserService userService;

    public List<User> getFollowersByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollower)
                .collect(Collectors.toList());
    }

    public List<UserSummary> getFollowersUserSummariesByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollowerUserSummary)
                .collect(Collectors.toList());
    }

    public boolean followUser(Long currentUserId, String userNameOfFollowedUser) {
        User currUser = userService.getUserById(currentUserId).orElse(null);
        User followedUser = userService.getUserByUsername(userNameOfFollowedUser).orElse(null);
        boolean isSuccess = false;

        if (followedUser != null && currUser != null) {
            followerRepository.save(new Follower(followedUser.getId(), currUser));
            isSuccess = true;
        }

        return isSuccess;
    }

    public boolean unfollowUser(Long currentUserId, String userNameOfFollowedUser) {
        User currUser = userService.getUserById(currentUserId).orElse(null);
        User followedUser = userService.getUserByUsername(userNameOfFollowedUser).orElse(null);
        boolean isSuccess = false;

        if (followedUser != null && currUser != null) {
            followerRepository.findByFollowedUserIdAndFollower(followedUser.getId(), currUser)
                    .forEach(followerRepository::delete);
            isSuccess = true;
        }

        return isSuccess;
    }

    public boolean isAlreadyFollowed(Long currentUserId, String userNameOfFollowedUser) {
        User currUser = userService.getUserById(currentUserId).orElse(null);
        User followedUser = userService.getUserByUsername(userNameOfFollowedUser).orElse(null);
        boolean isAlreadyFollowed = false;

        if (followedUser != null && currUser != null) {
            for (Follower follower : followerRepository.findByFollower(currUser)) {
                if (follower.getFollowedUserId().equals(followedUser.getId()) && follower.getFollower().equals(currUser)) {
                    isAlreadyFollowed = true;
                    break;
                }
            }
        }

        return isAlreadyFollowed;
    }
}
