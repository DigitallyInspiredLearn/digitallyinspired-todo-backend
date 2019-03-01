package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UpdatingUserInput;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TodoListRepository todoListRepository;
    private final ShareRepository shareRepository;
    private final FollowerRepository followerRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository repository, TodoListRepository todoListRepository,
                       ShareRepository shareRepository, FollowerRepository followerRepository, @Lazy BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = repository;
        this.todoListRepository = todoListRepository;
        this.shareRepository = shareRepository;
        this.followerRepository = followerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserSummary getUserInfo(UserPrincipal user) {
        return new UserSummary(user.getUsername(), user.getName(), user.getEmail());
    }

    public UserStats getUserStats(Long userId) {
        List<TodoList> myTodoLists = todoListRepository.findTodoListsByUserOwnerId(userId);
        List<TodoList> sharedTodoLists = shareRepository.findBySharedUserId(userId)
                .stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
        return new UserStats(myTodoLists, sharedTodoLists);
    }

    public Set<String> searchUsersByPartOfUsername(String partOfUsername) {
        return userRepository.findByUsernameLike(partOfUsername + "%")
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public Optional<User> updateUser(Long userId, UpdatingUserInput userInput) {
        Optional<User> user = userRepository.findById(userId)
                .map(u -> {
                    if (userInput.getName() != null) {
                        u.setName(userInput.getName());
                    }
                    if (userInput.getUsername() != null) {
                        u.setUsername(userInput.getUsername());
                    }
                    if (userInput.getEmail() != null) {
                        u.setEmail(userInput.getEmail());
                    }
                    if (userInput.getPassword() != null) {
                        u.setPassword(bCryptPasswordEncoder.encode(userInput.getPassword()));
                    }
                    return u;
                });

        User updatedUser = null;

        if (user.isPresent()) {
            updatedUser = userRepository.save(user.get());
        }

        return Optional.ofNullable(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null){
            followerRepository.findByFollower(user).forEach(followerRepository::delete);
            followerRepository.findByFollowedUserId(id).forEach(followerRepository::delete);
            todoListRepository.findTodoListsByUserOwnerId(user.getId()).forEach(todoListRepository::delete);
            userRepository.deleteById(id);
        }
    }

    public UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail)
                );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id : " + id)
        );

        return UserPrincipal.create(user);
    }
}
