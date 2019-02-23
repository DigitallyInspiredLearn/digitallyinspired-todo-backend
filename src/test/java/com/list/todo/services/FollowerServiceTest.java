package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import com.list.todo.repositories.FollowerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class FollowerServiceTest {

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private FollowerService followerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getFollowersByUserId() {
        List<Follower> followerList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        long userId = 1;
        User user1 = new User("name", "username", "email", "password");
        User user2 = new User("name1", "username1", "email1", "password1");
        followerList.add(new Follower(userId, user1));
        followerList.add(new Follower(userId, user2));
        userList.add(user1);
        userList.add(user2);

        when(followerRepository.findByFollowedUserId(userId)).thenReturn(followerList);
        List<User> returnedUsers = followerService.getFollowersByUserId(userId);
        Assert.assertEquals(returnedUsers, userList);
    }

    @Test
    public void getFollowersUserSummariesByUserId() {
    }

    @Test
    public void followUser() {
    }
}