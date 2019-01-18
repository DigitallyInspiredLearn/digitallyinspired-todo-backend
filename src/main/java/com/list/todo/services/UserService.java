package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    
	private final UserRepository userRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository repository, @Lazy BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long userId){
        return userRepository.findById(userId).get();
    }

    public void saveUser(User user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    
    public void updateUser(User user) {
    	userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
    	userRepository.deleteById(id);
    }

    public boolean isUserExist(User user){
        for (User u : getAllUsers()){
            if (u.equals(user)){
                return true;
            }
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String eMail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(eMail);

        if (user == null){
            throw new UsernameNotFoundException("User not found");
        }
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("user"));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}
