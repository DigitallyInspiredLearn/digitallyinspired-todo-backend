package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final UserService userService;

    @Value("${gravatar.url}")
    private String gravatarURL;

    public List<User> getFollowersByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollower)
                .collect(Collectors.toList());
    }

    public List<UserSummary> getFollowersUserSummariesByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(this::getFollowerUserSumm)
                .collect(Collectors.toList());
    }

    public UserSummary getFollowerUserSumm(Follower follower) {
        User user = follower.getFollower();
        return new UserSummary(user.getUsername(), user.getName(), user.getEmail(), gravatarURL + user.getGravatarHash());
    }

    public boolean followUser(Long currentUserId, String userNameOfFollowedUser) {
        User currUser = userService.getUserById(currentUserId).orElse(null);
        User followedUser = userService.getUserByUsername(userNameOfFollowedUser).orElse(null);
        boolean isSuccess = false;

        if (followedUser != null) {
            followerRepository.save(new Follower(followedUser.getId(), currUser));
            isSuccess = true;
        }

        return isSuccess;
    }

}
