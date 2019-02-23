package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.*;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class UserQuery implements GraphQLQueryResolver {

    private FollowerService followerService;
	private UserService userService;

    public UserSummary getUserInfo() {
        UserPrincipal user = userService.getCurrentUser();
        return userService.getUserInfo(user);
    }

    public Set<String> searchUserNames(String partOfUserName) {
        return userService.searchUsersByPartOfUsername(partOfUserName);
    }

    public UserStats getUserStats() {
        UserPrincipal user = userService.getCurrentUser();
        return userService.getUserStats(user.getId());
    }

    public List<UserSummary> getFollowers() {
        UserPrincipal user = userService.getCurrentUser();
        return followerService.getFollowersUserSummariesByUserId(user.getId());
    }

}
