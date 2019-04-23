package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.UserInput;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.JwtTokenProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import static com.list.todo.util.ObjectsProvider.createUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuthenticationServiceTest {

    private final static String CURRENT_EMAIL = "exmp@g.com";
    private final static String CURRENT_USERNAME = "username";
    private final static String CURRENT_PASSWORD = "password";
    private final static String CURRENT_NAME = "name";
    private final static String ANOTHER_USERNAME = "usr";
    private final static String ANOTHER_EMAIL = "aa@g.com";

    @Mock
    private AuthenticationManager authenticationManagerMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @Mock
    private JwtTokenProvider jwtTokenProviderMock;

    @InjectMocks
    private AuthenticationService authenticationServiceMock;


    @Test
    public void authenticateUser_SuccessfulAuthentication_ReturnsAJwtToken() {
        //arrange
        LoginRequest loginRequest = new LoginRequest(CURRENT_USERNAME, CURRENT_PASSWORD);
        Authentication authentication = mock(Authentication.class);
        String token = "fjfgj";

        when(authenticationManagerMock.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                ))).thenReturn(authentication);

        when(jwtTokenProviderMock.generateToken(authentication)).thenReturn(token);

        //act
        JwtAuthenticationResponse response = authenticationServiceMock.authenticateUser(loginRequest);

        //assert
        assertEquals(response.getAccessToken(), token);
        verify(jwtTokenProviderMock, times(1)).generateToken(authentication);
    }

    @Test
    @WithMockUser(username = CURRENT_USERNAME)
    public void updateAccessToken_SuccessfulUpdate_ReturnAUpdatedToken() {
        //arrange
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = "fjfgj";

        when(jwtTokenProviderMock.generateToken(authentication)).thenReturn(token);

        //act
        JwtAuthenticationResponse response = authenticationServiceMock.updateAccessToken();

        //assert
        assertEquals(response.getAccessToken(), token);
        verify(jwtTokenProviderMock, times(1)).generateToken(authentication);

    }

    @Test
    public void createUserAccount_SuccessfulCreate_ReturnsACreatedUser() {
        //arrange
        UserInput userInput = UserInput.builder()
                .username(CURRENT_USERNAME)
                .password(CURRENT_PASSWORD)
                .email(CURRENT_EMAIL)
                .name(CURRENT_NAME)
                .build();

        User user = createUser(userInput);

        String encodedPassword = new BCryptPasswordEncoder().encode(userInput.getPassword());

        when(passwordEncoderMock.encode(CURRENT_PASSWORD)).thenReturn(encodedPassword);
        when(userRepositoryMock.save(any(User.class))).thenReturn(user);

        //act
        User userFromService = authenticationServiceMock.createUserAccount(userInput);

        //assert
        assertEquals(userFromService, user);
    }

    @Test
    public void isUserExistByUserName_OnExistentUser_ReturnsATrue() {
        //arrange
        when(userRepositoryMock.existsByUsername(CURRENT_USERNAME)).thenReturn(true);

        //act
        boolean isUserExist = authenticationServiceMock.isUserExistByUserName(CURRENT_USERNAME);

        //assert
        assertTrue(isUserExist);
        verify(userRepositoryMock, times(1)).existsByUsername(CURRENT_USERNAME);
    }

    @Test
    public void isUserExistByUserName_OnNonExistentUser_ReturnsAFalse() {
        //arrange
        when(userRepositoryMock.existsByUsername(ANOTHER_USERNAME)).thenReturn(false);

        //act
        boolean isUserExist = authenticationServiceMock.isUserExistByUserName(ANOTHER_USERNAME);

        //assert
        assertFalse(isUserExist);
        verify(userRepositoryMock, times(1)).existsByUsername(ANOTHER_USERNAME);
    }

    @Test
    public void isUserExistByEmail_OnExistentUser_ReturnsATrue() {
        //arrange
        when(userRepositoryMock.existsByEmail(CURRENT_EMAIL)).thenReturn(true);

        //act
        boolean isUserExist = authenticationServiceMock.isUserExistByEmail(CURRENT_EMAIL);

        //assert
        assertTrue(isUserExist);
        verify(userRepositoryMock, times(1)).existsByEmail(CURRENT_EMAIL);
    }

    @Test
    public void isUserExistByEmail_OnNonExistentUser_ReturnsAFalse() {
        //arrange
        when(userRepositoryMock.existsByEmail(ANOTHER_EMAIL)).thenReturn(false);

        //act
        boolean isUserExist = authenticationServiceMock.isUserExistByEmail(ANOTHER_EMAIL);

        //assert
        assertFalse(isUserExist);
        verify(userRepositoryMock, times(1)).existsByEmail(ANOTHER_EMAIL);
    }
}