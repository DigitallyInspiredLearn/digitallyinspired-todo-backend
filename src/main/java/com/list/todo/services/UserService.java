package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
	private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository repository) {
        this.userRepository = repository;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long userId){
        return userRepository.findById(userId).get();
    }

    public void saveUser(User user){
        userRepository.save(user);
    }
    
    public void updateUser(User user) {
    	userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
    	userRepository.deleteById(id);
    }
    
}
