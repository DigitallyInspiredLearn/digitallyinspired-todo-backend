package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final UserService userService;

    @Value("${gravatar.url}")
    private String gravatarURL;

    List<User> getFollowersByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(Follower::getFollower)
                .collect(Collectors.toList());
    }

    public List<UserSummary> getFollowedUserSummariesByUserId(Long userId) {
        AtomicReference<List<UserSummary>> followedUserSummaries = new AtomicReference<>();

        userService.getUserById(userId).ifPresent(user -> {
            followedUserSummaries.set(followerRepository.findByFollower(user)
                    .stream()
                    .map(this::getFollowedUserSummary)
                    .collect(Collectors.toList()));
        });
        return followedUserSummaries.get();
    }

    public List<UserSummary> getFollowersUserSummariesByUserId(Long userId) {
        return followerRepository.findByFollowedUserId(userId)
                .stream()
                .map(this::getFollowerUserSummary)
                .collect(Collectors.toList());
    }

    private UserSummary getFollowedUserSummary(Follower follower) {
        AtomicReference<UserSummary> userSummary = new AtomicReference<>();

        userService.getUserById(follower.getFollowedUserId())
                .ifPresent(user -> userSummary.set(
                        UserSummary.builder()
                                .username(user.getUsername())
                                .name(user.getName())
                                .email(user.getEmail())
                                .gravatarUrl(gravatarURL + user.getGravatarHash())
                                .build()
                ));

        return userSummary.get();
    }

    private UserSummary getFollowerUserSummary(Follower follower) {
        User user = follower.getFollower();
        return UserSummary.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .gravatarUrl(gravatarURL + user.getGravatarHash())
                .build();
    }

    public boolean followUser(Long currentUserId, String userNameOfFollowedUser) {

        AtomicBoolean isSuccess = new AtomicBoolean(false);
        userService.getUserById(currentUserId).ifPresent(currentUser ->
                userService.getUserByUsername(userNameOfFollowedUser).ifPresent(followedUser -> {
                    followerRepository.save(new Follower(followedUser.getId(), currentUser));
                    isSuccess.set(true);
                }));

        return isSuccess.get();
    }

    public boolean unfollowUser(Long currentUserId, String userNameOfFollowedUser) {
        AtomicBoolean isSuccess = new AtomicBoolean(false);

        userService.getUserById(currentUserId).ifPresent(currentUser ->
                userService.getUserByUsername(userNameOfFollowedUser).ifPresent(followedUser -> {
                    followerRepository.findByFollowedUserIdAndFollower(followedUser.getId(), currentUser)
                            .forEach(followerRepository::delete);
                    isSuccess.set(true);
                }));

        return isSuccess.get();
    }

    public boolean isAlreadyFollowed(Long currentUserId, String userNameOfFollowedUser) {

        AtomicBoolean isAlreadyFollowed = new AtomicBoolean(false);


        userService.getUserById(currentUserId).ifPresent(currentUser ->
                userService.getUserByUsername(userNameOfFollowedUser).ifPresent(followedUser ->
                        followerRepository.findByFollower(currentUser)
                                .forEach(follower -> {
                                    if (follower.getFollowedUserId().equals(followedUser.getId()) &&
                                            follower.getFollower().equals(currentUser)) {
                                        isAlreadyFollowed.set(true);
                                    }
                                })));

        return isAlreadyFollowed.get();
    }
}
