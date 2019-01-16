package com.list.todo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
}
