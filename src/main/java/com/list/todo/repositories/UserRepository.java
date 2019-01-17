package com.list.todo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.User;

import javax.validation.constraints.Email;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    User findByEMail(@Email String eMail);
}
