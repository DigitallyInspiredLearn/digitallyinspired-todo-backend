package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.*;
import com.list.todo.security.UserPrincipal;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class UserQuery implements GraphQLQueryResolver {

    private UserRepository userRepository;
    private FollowerRepository followerRepository;
	private TodoListRepository todoListRepository;
	private ShareRepository shareRepository;

    public UserSummary getUserInfo() {
        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new UserSummary(user.getUsername(), user.getName(), user.getEmail());
    }

    public Set<String> searchUserNames(String partOfUserName) {
        return userRepository.findByUsernameLike(partOfUserName + "%").stream()
                .map(User::getUsername)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public UserStats getUserStats() {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<TodoList> myTodoLists = todoListRepository.findTodoListsByUserOwnerId(user.getId());
        List<TodoList> sharedTodoLists = shareRepository.findBySharedUserId(user.getId()).stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
        return new UserStats(myTodoLists, sharedTodoLists);
    }

    public List<UserSummary> getFollowers() {
        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return followerRepository.findByFollowedUserId(user.getId()).stream()
                .map(f -> new UserSummary(f.getFollower().getUsername(), f.getFollower().getName(), f.getFollower().getEmail()))
                .collect(Collectors.toList());
    }

}
