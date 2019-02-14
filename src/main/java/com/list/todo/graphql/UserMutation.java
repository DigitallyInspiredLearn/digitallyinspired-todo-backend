package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.UserInput;
import com.list.todo.repositories.*;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.AuthenticationService;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMutation implements GraphQLMutationResolver {

    private AuthenticationService authenticationService;
    private UserService userService;
    private FollowerService followerService;

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        return authenticationService.authenticateUser(loginRequest);
    }

    public ApiResponse register(UserInput userInput) {
        if (authenticationService.isUserExistByUserName(userInput.getUsername())) {
            return new ApiResponse(false, "Username is already taken!");
        }

        if (authenticationService.isUserExistByEmail(userInput.getEmail())) {
            return new ApiResponse(false, "Email Address already in use!");
        }

        authenticationService.createUserAccount(userInput);

        return new ApiResponse(true, "User registered successfully");

    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public User updateMyProfile(UserInput userInput) {
        UserPrincipal user = userService.getCurrentUser();
        return userService.updateUser(user.getId(), userInput);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ApiResponse deleteMyProfile() {
        UserPrincipal user = userService.getCurrentUser();
        userService.deleteUser(user.getId());
        return new ApiResponse(true, "Profile is deleted successfully!");
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ApiResponse followUser(String userNameOfFollowedUser) {

        UserPrincipal user = userService.getCurrentUser();
        ApiResponse apiResponse;

        if (followerService.followUser(user.getId(), userNameOfFollowedUser)) {
            apiResponse = new ApiResponse(true, "You'll follow this user!");
        } else {
            apiResponse = new ApiResponse(false, "You can't follow this user!");
        }

        return apiResponse;
    }
}
