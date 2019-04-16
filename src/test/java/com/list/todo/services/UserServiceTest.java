package com.list.todo.services;

import com.list.todo.entity.*;
import com.list.todo.payload.UpdatingUserInput;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShareRepository shareRepository;

    @Mock
    private TodoListRepository todoListRepository;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private Pageable pageable;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private Long currentUserId = 1L;

    @Test
    public void getUserById_OnExistentUser_ReturnsOptionalOfUser() {
        //arrange
        User user = createUser(1);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

        //act
        Optional<User> returnedUser = userService.getUserById(currentUserId);

        //assert
        verify(userRepository).findById(currentUserId);
        Assert.assertEquals(user, returnedUser.get());
    }

    @Test
    public void getUserByUsername_OnExistentUser_ReturnsOptionalOfUser() {
        //arrange
        User user = createUser(1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        //act
        Optional<User> returnedUser = userService.getUserByUsername(user.getUsername());

        //assert
        verify(userRepository).findByUsername(user.getUsername());
        Assert.assertEquals(user, returnedUser.get());
    }

    @Test
    public void getUserInfo_OnExistentUser_ReturnsAnObjectOfUserSummary() {
        //arrange
        User user = createUser(1);
        UserSummary userSummary = new UserSummary(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getGravatarHash());
        UserPrincipal userPrincipal = new UserPrincipal(
                currentUserId,
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(user));

        //act
        UserSummary returnedUserSummary = userService.getUserInfo(userPrincipal);

        //assert
        verify(userRepository).findById(userPrincipal.getId());
        Assert.assertEquals(userSummary.getName(), returnedUserSummary.getName());
        Assert.assertEquals(userSummary.getEmail(), returnedUserSummary.getEmail());
        Assert.assertEquals(userSummary.getUsername(), returnedUserSummary.getUsername());
    }

    @Test
    public void getUserStats_OnExistentUser_ReturnsAnObjectOfUserStats() {
        //arrange
        UserPrincipal userPrincipal = new UserPrincipal(
                currentUserId,
                "name",
                "username",
                "email",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        List<Share> shares = new ArrayList<>();
        Share share1 = new Share(userPrincipal.getId(), new TodoList());
        Share share2 = new Share(userPrincipal.getId(), new TodoList());
        shares.add(share1);
        shares.add(share2);
        List<TodoList> sharedTodoLists = shares.stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
        List<TodoList> myTodoLists = new ArrayList<>();
        TodoList todoList1 = new TodoList();
        TodoList todoList2 = new TodoList();
        myTodoLists.add(todoList1);
        myTodoLists.add(todoList2);

        Page<TodoList> sharedTodoListsPage = new PageImpl<>(sharedTodoLists, pageable, sharedTodoLists.size());
        Page<TodoList> myTodoListsPage = new PageImpl<>(myTodoLists, pageable, myTodoLists.size());
        when(shareRepository.findBySharedUserId(userPrincipal.getId())).thenReturn(shares);
        when(todoListRepository.findByCreatedByAndTodoListStatus(userPrincipal.getUsername(), TodoListStatus.ACTIVE, pageable))
                .thenReturn(myTodoListsPage);

        //act
        UserStats returnedUserStats = userService.getUserStats(userPrincipal, pageable);

        //assert
        verify(shareRepository).findBySharedUserId(userPrincipal.getId());
        verify(todoListRepository).findByCreatedByAndTodoListStatus(userPrincipal.getUsername(), TodoListStatus.ACTIVE, pageable);
        Assert.assertEquals(myTodoListsPage, returnedUserStats.getMyTodoLists());
        Assert.assertEquals(sharedTodoListsPage, returnedUserStats.getSharedTodoLists());
    }

    @Test
    public void searchUsersByPartOfUsername_OnExistentUser_ReturnsASetOfStrings() {
        //arrange
        String partOfUsername = "us";
        List<User> users = new ArrayList<>();
        User user1 = createUser(1);
        User user2 = createUser(2);
        users.add(user1);
        users.add(user2);
        Set<String> usernames = new HashSet<>();
        usernames.add(user1.getUsername());
        usernames.add(user2.getUsername());

        when(userRepository.findByUsernameLike(partOfUsername + "%")).thenReturn(users);

        //act
        Set<String> returnedUsernames = userService.searchUsersByPartOfUsername(partOfUsername);

        //assert
        verify(userRepository).findByUsernameLike(partOfUsername + "%");
        Assert.assertEquals(usernames, returnedUsernames);
    }

    @Test
    public void updateUser_OnExistentUser_ReturnsAnObjectOfUser() {
        //arrange
        User user = createUser(1);
        User updatedUser = createUser(2);
        UpdatingUserInput userInput = new UpdatingUserInput();
        userInput.setName(updatedUser.getName());
        userInput.setUsername(updatedUser.getUsername());
        userInput.setEmail(updatedUser.getEmail());
        userInput.setPassword(updatedUser.getPassword());
        user = spy(user);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.encode(updatedUser.getPassword())).thenReturn(updatedUser.getPassword());
        when(userRepository.save(user)).thenReturn(updatedUser);

        //act
        Optional<User> returnedUser = userService.updateUser(currentUserId, userInput);

        //assert
        verify(user).setName(userInput.getName());
        verify(user).setUsername(userInput.getUsername());
        verify(user).setEmail(userInput.getEmail());
        verify(user).setPassword(userInput.getPassword());
        Assert.assertEquals(updatedUser, returnedUser.get());
    }

    @Test
    public void deleteUser_OnExistentUser_ReturnsVoid() {
        //arrange
        User user = createUser(1);
        List<Follower> followers = new ArrayList<>();
        followers.add(new Follower());
        followers.add(new Follower());
        List<Follower> followedUsers = new ArrayList<>();
        followedUsers.add(new Follower());
        followedUsers.add(new Follower());
        List<TodoList> todoLists = new ArrayList<>();
        todoLists.add(new TodoList());
        todoLists.add(new TodoList());

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
        when(followerRepository.findByFollower(user)).thenReturn(followedUsers);
        when(followerRepository.findByFollowedUserId(currentUserId)).thenReturn(followers);
        when(todoListRepository.findByCreatedBy(user.getUsername())).thenReturn(todoLists);

        //act
        userService.deleteUser(currentUserId);

        //assert
        verify(userRepository).findById(currentUserId);
        verify(followerRepository).findByFollower(user);
        verify(followerRepository).findByFollowedUserId(currentUserId);
        verify(todoListRepository).findByCreatedBy(user.getUsername());
        verify(followerRepository, times(followedUsers.size()+followers.size())).delete(any(Follower.class));
        verify(todoListRepository, times(todoLists.size())).delete(any(TodoList.class));
    }


    private User createUser(int postfixNumber) {
        return new User(
                "name" + postfixNumber,
                "username" + postfixNumber,
                "email" + postfixNumber,
                "password" + postfixNumber,
                "gravatarHash" + postfixNumber);
    }
}