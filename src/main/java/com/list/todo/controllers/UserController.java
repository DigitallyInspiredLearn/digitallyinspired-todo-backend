package com.list.todo.controllers;

import com.list.todo.entity.User;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.UserInput;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class UserController {

    private final UserService userService;
    private final FollowerService followerService;

    @GetMapping("/me")
    public ResponseEntity<UserSummary> getUserInfo(@AuthenticationPrincipal UserPrincipal currentUser) {
        return new ResponseEntity<>(userService.getUserInfo(currentUser), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Set<String>> searchUserNamesByPartOfUserName(@RequestParam("username") String username) {
        return new ResponseEntity<>(userService.searchUsersByPartOfUsername(username), HttpStatus.OK);
    }

    @GetMapping("/userStats")
    public ResponseEntity<UserStats> getUserStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        return new ResponseEntity<>(userService.getUserStats(currentUser.getId()), HttpStatus.OK);
    }

    @PutMapping("/editProfile")
    public ResponseEntity<User> updateMyProfile(@AuthenticationPrincipal UserPrincipal currentUser,
                                                @RequestBody UserInput userInput) {
        User user = userService.updateUser(currentUser.getId(), userInput);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<User> deleteMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        // TODO: девалидация токена
        userService.deleteUser(currentUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/followUser")
    public ResponseEntity<ApiResponse> followUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                  @RequestParam("username") String userNameOfFollowedUser) {
        ResponseEntity<ApiResponse> responseEntity;

        if (followerService.followUser(currentUser.getId(), userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(new ApiResponse(true, "You'll follow this user!"), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(new ApiResponse(false, "You can't follow this user!"), HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserSummary>> getFollowers(@AuthenticationPrincipal UserPrincipal currentUser) {
        return new ResponseEntity<>(followerService.getFollowersUserSummariesByUserId(currentUser.getId()), HttpStatus.OK);
    }
}
