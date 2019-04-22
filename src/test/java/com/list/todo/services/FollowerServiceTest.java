package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.util.MD5Util;
import com.list.todo.util.ObjectsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FollowerServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private FollowerService followerService;

    private static final Long USER_ID = 1L;
    private static final String USER_USERNAME = "username";
    private static final Long SECOND_USER_ID = 2L;

    @Test
    public void getFollowersByUserId_GetUserFollowers_ListOfUserFollowers() {
        // arrange
        List<Follower> followerList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        User user1 = new User();
        User user2 = new User();
        followerList.add(new Follower(USER_ID, user1));
        followerList.add(new Follower(SECOND_USER_ID, user2));
        userList.add(user1);
        userList.add(user2);

        when(followerRepository.findByFollowedUserId(USER_ID)).thenReturn(followerList);

        // act
        List<User> returnedUsers = followerService.getFollowersByUserId(USER_ID);

        // assert
        Assert.assertEquals(returnedUsers, userList);
    }

    @Test
    public void getFollowedUserSummariesByUserId_GetUserSummariesOfUserFollowers_ListOfUserSummariesOfUserFollowers() {
        // arrange
        User user = new User();
        List<Follower> followers = createListOfFollowers();
        List<UserSummary> userSummaries = createListOfUserSummaries();

        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));
        when(followerRepository.findByFollower(user)).thenReturn(followers);

        // act
        List<UserSummary> result = followerService.getFollowedUserSummariesByUserId(USER_ID);

        // assert
        Assert.assertEquals(userSummaries, result);
        verify(userService, times(3)).getUserById(USER_ID);
        verify(followerRepository, times(1)).findByFollower(user);
    }

    @Test
    public void getFollowersUserSummariesByUserId_GetUserSummariesOfUserFollowers_ListOfUserSummariesOfUserFollowers() {
        // arrange
        List<UserSummary> userSummaries = createListOfUserSummaries();
        List<Follower> followers = createListOfFollowers();

        when(followerRepository.findByFollowedUserId(USER_ID)).thenReturn(followers);

        // act
        List<UserSummary> result = followerService.getFollowersUserSummariesByUserId(USER_ID);

        // assert
        Assert.assertEquals(userSummaries, result);
        verify(followerRepository, times(1)).findByFollowedUserId(USER_ID);
    }

    @Test
    public void followUser_FollowUserByUserId_IsSuccess() {
        // arrange
        boolean isSuccess = true;
        User user = createUser(Math.toIntExact(USER_ID));
        User followedUser = createUser(Math.toIntExact(SECOND_USER_ID));

        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));
        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(followedUser));

        // act
        boolean result = followerService.followUser(USER_ID, USER_USERNAME);

        // assert
        Assert.assertEquals(isSuccess, result);
        verify(userService, times(1)).getUserById(USER_ID);
        verify(userService, times(1)).getUserByUsername(USER_USERNAME);
        verify(followerRepository, times(1)).save(new Follower(followedUser.getId(), user));
    }

    @Test
    public void unfollowUser_UnfollowUserByUserId_IsSuccess() {
        // arrange
        boolean isSuccess = true;
        User user = createUser(Math.toIntExact(USER_ID));
        User followedUser = createUser(Math.toIntExact(SECOND_USER_ID));
        List<Follower> followers = createListOfFollowers();

        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));
        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(followedUser));
        when(followerRepository.findByFollowedUserIdAndFollower(followedUser.getId(), user)).thenReturn(followers);

        // act
        boolean result = followerService.unfollowUser(USER_ID, USER_USERNAME);

        // assert
        Assert.assertEquals(isSuccess, result);
        verify(userService, times(1)).getUserById(USER_ID);
        verify(userService, times(1)).getUserByUsername(USER_USERNAME);
        verify(followerRepository, times(1)).findByFollowedUserIdAndFollower(followedUser.getId(), user);
        verify(followerRepository, times(2)).delete(any(Follower.class));
    }

    @Test
    public void isAlreadyFollowed_CheckIfTheUserIsFollowed_IsSuccess() {
        // arrange
        boolean isSuccess = true;
        User user = new User();
        User followedUser = createUser(Math.toIntExact(SECOND_USER_ID));
        followedUser.setId(USER_ID);
        List<Follower> followers = createListOfFollowers();
        followers.forEach(follower -> follower.setFollower(user));

        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));
        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(followedUser));
        when(followerRepository.findByFollower(user)).thenReturn(followers);

        // act
        boolean result = followerService.isAlreadyFollowed(USER_ID, USER_USERNAME);

        // assert
        Assert.assertEquals(isSuccess, result);
        verify(userService, times(1)).getUserById(USER_ID);
        verify(userService, times(1)).getUserByUsername(USER_USERNAME);
        verify(followerRepository, times(1)).findByFollower(user);

    }
}