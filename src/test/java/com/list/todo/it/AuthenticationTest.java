package com.list.todo.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.TodoListApplication;
import com.list.todo.configurations.H2TestProfileJPAConfig;
import com.list.todo.controllers.AuthenticationController;
import com.list.todo.entity.User;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.UserInput;
import com.list.todo.services.AuthenticationService;
import com.list.todo.services.UserSettingsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.list.todo.util.ObjectsProvider.createUser;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TodoListApplication.class, H2TestProfileJPAConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthenticationTest {

    private final static String CURRENT_EMAIL = "exmp@g.com";
    private final static String CURRENT_USERNAME = "username";
    private final static String CURRENT_PASSWORD = "password";
    private final static String CURRENT_NAME = "name";
    private final static String ANOTHER_USERNAME = "usr";
    private final static String ANOTHER_EMAIL = "aa@g.com";
    private final static String ANOTHER_PASSWORD = "pas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Mock
    private AuthenticationService authenticationServiceMock;

    @Mock
    private UserSettingsService userSettingsServiceMock;

    @InjectMocks
    private AuthenticationController authenticationControllerMock;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authenticationControllerMock)
                .apply(springSecurity(springSecurityFilterChain))
                .build();
    }

    @Test
    public void registerUser_OnSuccessfulRegister_ReturnsAIsCreated() throws Exception {
        //arrange
        UserInput userInput = UserInput.builder()
                .username(CURRENT_USERNAME)
                .password(CURRENT_PASSWORD)
                .email(CURRENT_EMAIL)
                .name(CURRENT_NAME)
                .build();

        User user = createUser(userInput);

        when(authenticationServiceMock.createUserAccount(userInput)).thenReturn(user);

        //act, assert
        this.mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(userInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("success").value("true"))
                .andExpect(status().isCreated());

        verify(authenticationServiceMock, times(1)).createUserAccount(userInput);
    }

    @Test
    public void registerUser_OnExistUsername_ReturnsAIsBadRequest() throws Exception {
        //arrange
        UserInput userInput = UserInput.builder()
                .username(CURRENT_USERNAME)
                .password(CURRENT_PASSWORD)
                .email(CURRENT_EMAIL)
                .name(CURRENT_NAME)
                .build();

        when(authenticationServiceMock.isUserExistByUserName(userInput.getUsername())).thenReturn(true);

        //act, assert
        this.mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(userInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("message").value("Username is already taken!"))
                .andExpect(status().isBadRequest());

        verify(authenticationServiceMock, never()).createUserAccount(userInput);
    }

    @Test
    public void registerUser_OnExistEmail_ReturnsAIsBadRequest() throws Exception {
        //arrange
        UserInput userInput = UserInput.builder()
                .username(CURRENT_USERNAME)
                .password(CURRENT_PASSWORD)
                .email(CURRENT_EMAIL)
                .name(CURRENT_NAME)
                .build();

        when(authenticationServiceMock.isUserExistByEmail(userInput.getEmail())).thenReturn(true);

        this.mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(userInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("message").value("Email Address already in use!"))
                .andExpect(status().isBadRequest());

        verify(authenticationServiceMock, never()).createUserAccount(userInput);
    }

    @Test
    @WithAnonymousUser
    public void authenticateUser_OnExistentUsername_ReturnsABearerToken() throws Exception {
        //arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(CURRENT_USERNAME)
                .password(CURRENT_PASSWORD)
                .build();

        String token = "fff";

        when(authenticationServiceMock.authenticateUser(loginRequest)).thenReturn(new JwtAuthenticationResponse(token));

        //act, assert
        this.mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("tokenType").value("Bearer"))
                .andExpect(status().isOk());

        verify(authenticationServiceMock, times(1)).authenticateUser(loginRequest);
    }

    @Test
    @WithAnonymousUser
    public void authenticateUser_OnExistentEmail_ReturnsABearerToken() throws Exception {
        //arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(CURRENT_EMAIL)
                .password(CURRENT_PASSWORD)
                .build();

        String token = "fff";

        when(authenticationServiceMock.authenticateUser(loginRequest)).thenReturn(new JwtAuthenticationResponse(token));

        //act, assert
        this.mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("tokenType").value("Bearer"))
                .andExpect(status().isOk());

        verify(authenticationServiceMock, times(1)).authenticateUser(loginRequest);
    }

    @Test
    @WithMockUser(username = ANOTHER_USERNAME)
    public void authenticateUser_OnWrongEmail_ReturnsAIsUnauthorized() throws Exception {
        //arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(ANOTHER_USERNAME)
                .password(CURRENT_PASSWORD)
                .build();

        when(authenticationServiceMock.authenticateUser(loginRequest)).thenThrow(BadCredentialsException.class);

        //act, assert
        this.mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(authenticationServiceMock, times(1)).authenticateUser(loginRequest);
    }

    @Test
    @WithMockUser(password = ANOTHER_PASSWORD)
    public void authenticateUser_OnWrongPassword_ReturnsAIsUnauthorized() throws Exception {
        //arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(ANOTHER_EMAIL)
                .password(CURRENT_PASSWORD)
                .build();

        when(authenticationServiceMock.authenticateUser(loginRequest)).thenThrow(BadCredentialsException.class);

        //act, assert
        this.mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(authenticationServiceMock, times(1)).authenticateUser(loginRequest);
    }
}
