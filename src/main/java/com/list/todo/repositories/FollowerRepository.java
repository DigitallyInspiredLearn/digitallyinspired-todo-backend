package com.list.todo.repositories;

import com.list.todo.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    public List<Follower> findByFollowedUserId(Long userId);
}