package com.list.todo.services;

import com.list.todo.entity.Follower;
import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.ShareRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FollowerService {

    private FollowerRepository followerRepository;

    public List<User> getFollowersByUserId(Long userId) {
        List<Follower> followers = followerRepository.findByFollowerUserId(userId);
        List<User> followedUsers = new ArrayList<>();
        followers
                .stream()
                .forEach(follower -> followedUsers.add(follower.getFollowedUser()));

        return followedUsers;
    }

    public void followUser(Follower follower) {
        followerRepository.save(follower);
    }
}
