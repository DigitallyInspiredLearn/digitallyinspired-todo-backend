package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

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
        return userService.getUserStats(user, Pageable.unpaged());
    }

    public List<UserSummary> getFollowers() {
        UserPrincipal user = userService.getCurrentUser();
        return followerService.getFollowersUserSummariesByUserId(user.getId());
    }

}
