package com.list.todo.repositories;

import com.list.todo.entity.Follower;
import com.list.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    List<Follower> findByFollowedUserId(Long userId);
    List<Follower> findByFollower(User follower);
    List<Follower> findByFollowedUserIdAndFollower(Long userId, User follower);
}