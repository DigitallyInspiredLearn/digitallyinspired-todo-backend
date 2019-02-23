package com.list.todo.services;

import com.list.todo.entity.RoleName;
import com.list.todo.entity.User;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.UserInput;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
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

    public User createUserAccount(UserInput userInput) {
        User user = new User(userInput.getName(), userInput.getUsername(),
                userInput.getEmail(), userInput.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleName.ROLE_USER);
        return userRepository.save(user);
    }

    public boolean isUserExistByUserName(String userName) {
        return userRepository.existsByUsername(userName);
    }

    public boolean isUserExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
