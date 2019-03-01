package com.list.todo.controllers;

import com.list.todo.entity.User;
import com.list.todo.payload.*;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<Optional<User>> updateMyProfile(@AuthenticationPrincipal UserPrincipal currentUser,
                                                          @RequestBody UpdatingUserInput userInput) {
        Optional<User> user = userService.updateUser(currentUser.getId(), userInput);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<User> deleteMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        userService.deleteUser(currentUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/followUser")
    public ResponseEntity<ApiResponse> followUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                  @RequestParam("username") String userNameOfFollowedUser) {
        ResponseEntity<ApiResponse> responseEntity;

        if (currentUser.getUsername().equals(userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (followerService.isAlreadyFollowed(currentUser.getId(), userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (followerService.followUser(currentUser.getId(), userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }

    @PostMapping("/unfollowUser")
    public ResponseEntity<ApiResponse> unfollowUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                  @RequestParam("username") String userNameOfFollowedUser) {
        ResponseEntity<ApiResponse> responseEntity;

        if (currentUser.getUsername().equals(userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (!followerService.isAlreadyFollowed(currentUser.getId(), userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (followerService.unfollowUser(currentUser.getId(), userNameOfFollowedUser)) {
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserSummary>> getFollowers(@AuthenticationPrincipal UserPrincipal currentUser) {
        return new ResponseEntity<>(followerService.getFollowersUserSummariesByUserId(currentUser.getId()), HttpStatus.OK);
    }
}
