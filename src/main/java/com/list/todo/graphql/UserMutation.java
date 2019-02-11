package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.RegisterRequest;
import com.list.todo.repositories.*;
import com.list.todo.security.JwtTokenProvider;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@AllArgsConstructor
public class UserMutation implements GraphQLMutationResolver {

    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;

    private UserRepository userRepository;
    private FollowerRepository followerRepository;

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new JwtAuthenticationResponse(jwt);
    }

    public ApiResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return new ApiResponse(false, "Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new ApiResponse(false, "Email Address already in use!");
        }

        // Creating user's account
        User user = new User(registerRequest.getName(), registerRequest.getUsername(),
                registerRequest.getEmail(), registerRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleName.ROLE_USER);
        User result = userRepository.save(user);

        return new ApiResponse(true, "User registered successfully");

    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public User updateMyProfile(String name, String username, String email, String password) {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updatedUser = userRepository.findById(user.getId()).orElse(null);

        if (updatedUser != null) {
            updatedUser.setName(name);
            updatedUser.setUsername(username);
            updatedUser.setEmail(email);
            updatedUser.setPassword(password);

            userRepository.save(updatedUser);
        }

        return updatedUser;
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ApiResponse deleteMyProfile() {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepository.deleteById(user.getId());
    return new ApiResponse(true, "Profile is deleted successfully!");
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ApiResponse followUser(String username) {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currUser = userRepository.findById(user.getId()).orElse(null);
        User followedUser = userRepository.findByUsername(username).orElse(null);
        ApiResponse apiResponse = new ApiResponse(false, "You can't follow this user!");

        if (followedUser != null) {
            Follower follower = new Follower(followedUser.getId(), currUser);
            followerRepository.save(follower);
            apiResponse = new ApiResponse(true, "You'll follow this user!");
        }

        return apiResponse;
    }
}
