package com.list.todo.it;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.controllers.UserController;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UpdatingUserInput;
import com.list.todo.payload.UserStatistics;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import com.list.todo.services.UserStatisticsService;
import com.list.todo.util.IdComparator;
import com.list.todo.utils.PageableStub;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private FollowerService followerService;

    @Mock
    private UserStatisticsService userStatisticsService;

    private PageableStub pageable = new PageableStub();

    private Long currentUserId = 1L;

    private ObjectMapper objectMapper = new ObjectMapper();

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(currentUserId);
            userPrincipal.setUsername("username");
            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("password"));

            return userPrincipal;
        }
    };

    private HandlerMethodArgumentResolver putPageable = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(
                MethodParameter parameter) {
            if (parameter.getParameterType().equals(
                    Pageable.class)) {
                return true;
            }
            return false;
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory) throws Exception {

            return new PageRequest(0, 50);
        }
    };

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
                .build();
    }


    @Test
    public void userInfo_OnExistentUser_ReturnsAnObjectOfUserSummary() throws Exception {
        //arrange
        when(userService.getUserInfo(any(UserPrincipal.class))).thenReturn(new UserSummary("stepanich", "Stepan Matveev", "stepa.matv72@gmail.com", null));

        //act, assert
        this.mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(jsonPath("name").value("Stepan Matveev"))
                .andExpect(jsonPath("username").value("stepanich"))
                .andExpect(jsonPath("email").value("stepa.matv72@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    public void serchUsersByUsername_OnExistentUser_ReturnsASetOfStrings() throws Exception {
        //arrange
        Set usernames = new HashSet();
        usernames.add("anna");
        when(userService.searchUsersByPartOfUsername(anyString())).thenReturn(usernames);

        //act, assert
        this.mockMvc.perform(get("/api/users/search?username={username}", "ann"))
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().json("[\"anna\"]"))
                .andExpect(status().isOk());

    }

    @Test
    public void searchUsersByNonExistentUsername_ReturnsAnEmptySet() throws Exception {
        //arrange
        Set usernames = new HashSet();
        when(userService.searchUsersByPartOfUsername(anyString())).thenReturn(usernames);

        //act, assert
        this.mockMvc.perform(get("/api/users/search?username={username}", "kkkkk"))
                .andDo(print())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(status().isOk());
    }

    @Test
    public void getUserStats_OnExistentUser_ReturnsAnObjectOfUserStats() throws Exception {
        //arrange
        int numberOfTodoLists = 2;
        List<TodoList> todoLists1 = this.createListOfTodoLists(numberOfTodoLists, "username1");
        List<TodoList> todoLists2 = this.createListOfTodoLists(numberOfTodoLists, "username2");
        Page<TodoList> myTodoLists = new PageImpl<>(todoLists1, pageable, todoLists1.size());
        Page<TodoList> sharedTodoLists = new PageImpl<>(todoLists2, pageable, todoLists2.size());
        UserStats userStats = new UserStats(myTodoLists, sharedTodoLists);
        when(userService.getUserStats(any(UserPrincipal.class), any(Pageable.class))).thenReturn(userStats);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/userStats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> returnedMyTodoLists = getTodoListsFromJsonResponse(
                result.getResponse().getContentAsString(),
                "myTodoLists");
        List<TodoList> returnedSharedTodoLists = getTodoListsFromJsonResponse(
                result.getResponse().getContentAsString(),
                "sharedTodoLists");

        IdComparator idComparator = new IdComparator();
        todoLists1.sort(idComparator);
        todoLists2.sort(idComparator);
        returnedMyTodoLists.sort(idComparator);
        returnedSharedTodoLists.sort(idComparator);

        //assert
        Assert.assertEquals(todoLists1, returnedMyTodoLists);
        Assert.assertEquals(todoLists2, returnedSharedTodoLists);
    }

    @Test
    public void updateUser_OnExistentUser_ReturnsAnObjectOfUser() throws Exception {
        //arrange
        UpdatingUserInput userInput = new UpdatingUserInput();
        userInput.setName("Stepa Baklagan");
        userInput.setUsername("stepka");
        userInput.setEmail("stepa.matv72@gmail.com");
        userInput.setPassword("Secrett");
        when(userService.updateUser(currentUserId, userInput)).thenReturn(Optional.of(new User(
                userInput.getName(),
                userInput.getUsername(),
                userInput.getEmail(),
                userInput.getPassword(),
                "gravatarHash")));

        //act, assert
        this.mockMvc.perform(put("/api/users/editProfile")
                .content(objectMapper.writeValueAsString(userInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("name").value(userInput.getName()))
                .andExpect(jsonPath("username").value(userInput.getUsername()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUser_OnExistentUser_ReturnsStatusNoContent() throws Exception {
        //act
        this.mockMvc.perform(delete("/api/users/deleteProfile"))
                .andDo(print())
                .andExpect(status().isNoContent());

        //assert
        verify(userService).deleteUser(currentUserId);
    }

    @Test
    public void followUser_OnExistentUser_ReturnsStatusOk() throws Exception {
        //arrange
        String followedUsername = "anna";
        when(followerService.isAlreadyFollowed(currentUserId, followedUsername)).thenReturn(false);
        when(followerService.followUser(currentUserId, followedUsername)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isOk());

        //assert
        verify(followerService).isAlreadyFollowed(currentUserId, followedUsername);
        verify(followerService).followUser(currentUserId, followedUsername);
    }

    @Test
    public void follow_OnNonExistentUser_ReturnsStatusNotFound() throws Exception {
        //arrange
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(currentUserId, followedUsername)).thenReturn(false);
        when(followerService.followUser(currentUserId, followedUsername)).thenReturn(false);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isNotFound());

        //assert
        verify(followerService).isAlreadyFollowed(currentUserId, followedUsername);
        verify(followerService).followUser(currentUserId, followedUsername);
    }

    @Test
    public void followAlreadyFollowedUser_ReturnsStatusIsForbidden() throws Exception {
        //arrange
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(currentUserId, followedUsername)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isForbidden());

        //assert
        verify(followerService).isAlreadyFollowed(currentUserId, followedUsername);
        verify(followerService, times(0)).followUser(currentUserId, followedUsername);
    }

    @Test
    public void unfollowUser_OnExistentUser_ReturnsStatusOk() throws Exception {
        //arrange
        String followedUsername = "anna";
        when(followerService.isAlreadyFollowed(currentUserId, followedUsername)).thenReturn(true);
        when(followerService.unfollowUser(currentUserId, followedUsername)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isOk());

        //assert
        verify(followerService).isAlreadyFollowed(currentUserId, followedUsername);
        verify(followerService).unfollowUser(currentUserId, followedUsername);
    }

    @Test
    public void unfollow_OnNonExistentUser_ReturnsStatusIsForbidden() throws Exception {
        //arrange
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(currentUserId, followedUsername)).thenReturn(false);

        //act
        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isForbidden());

        //assert
        verify(followerService).isAlreadyFollowed(currentUserId, followedUsername);
        verify(followerService, times(0)).unfollowUser(currentUserId, followedUsername);
    }

    @Test
    public void getFollowers_OnExistentUser_ReturnsAListOfUserSummaries() throws Exception {
        //arrange
        List<UserSummary> userSummaries = new ArrayList<>();
        UserSummary userSummary1 = createUserSummary(1);
        UserSummary userSummary2 = createUserSummary(2);
        userSummaries.add(userSummary1);
        userSummaries.add(userSummary2);

        when(followerService.getFollowersUserSummariesByUserId(currentUserId)).thenReturn(userSummaries);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/followers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<UserSummary> returnedUserSummaries = getUserSummariesFromJsonResponse(result.getResponse().getContentAsString());
        Collections.sort(userSummaries);
        Collections.sort(returnedUserSummaries);

        //assert
        Assert.assertEquals(userSummaries, returnedUserSummaries);
    }

    @Test
    public void getFollowedUsers_OnExistentUser_ReturnsAListOfUserSummaries() throws Exception {
        //arrange
        List<UserSummary> userSummaries = new ArrayList<>();
        UserSummary userSummary1 = createUserSummary(1);
        UserSummary userSummary2 = createUserSummary(2);
        userSummaries.add(userSummary1);
        userSummaries.add(userSummary2);

        when(followerService.getFollowedUserSummariesByUserId(currentUserId)).thenReturn(userSummaries);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/followed"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<UserSummary> returnedUserSummaries = getUserSummariesFromJsonResponse(result.getResponse().getContentAsString());
        Collections.sort(userSummaries);
        Collections.sort(returnedUserSummaries);

        //assert
        Assert.assertEquals(userSummaries, returnedUserSummaries);
    }

    @Test
    public void getUserStatistics_OnExistentUser_ReturnsAnObjectOfUserStatistics() throws Exception {
        //arrange
        UserStatistics userStatistics = createUserStatistics();

        when(userStatisticsService.getUserStatisticsByUserId(currentUserId)).thenReturn(userStatistics);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserStatistics returnedUserStatistics = objectMapper
                .readValue(result.getResponse().getContentAsString(), UserStatistics.class);

        //assert
        Assert.assertEquals(userStatistics, returnedUserStatistics);
    }

    private List<TodoList> createListOfTodoLists(int countOfTodoLists, String createdBy) {
        List<TodoList> todoLists = new ArrayList<>(countOfTodoLists);

        for (long i = 0; i < countOfTodoLists; i++) {
            TodoList todoList = new TodoList();
            todoList.setTodoListName("name"+i);
            todoList.setCreatedBy(createdBy);
            todoList.setId(i);

            todoLists.add(todoList);
        }

        return todoLists;
    }

    private List<TodoList> getTodoListsFromJsonResponse(String response,String arrayName) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject myTodoListsPageJson = (JSONObject) jsonObject.get(arrayName);
        JSONArray myTodoListsJson = myTodoListsPageJson.getJSONArray("content");
        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, TodoList.class);

        return objectMapper.readValue(myTodoListsJson.toString(), type);
    }

    private List<UserSummary> getUserSummariesFromJsonResponse(String response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, UserSummary.class);

        return objectMapper.readValue(response, type);
    }

    private UserStatistics createUserStatistics() {
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setTodoListsNumber(21L);
        userStatistics.setTasksNumber(11L);
        userStatistics.setCompletedTasksNumber(5L);
        userStatistics.setFollowedUsersNumber(2);
        userStatistics.setFollowersNumber(3);

        return userStatistics;
    }

    private UserSummary createUserSummary(int postfixNumber) {
        return new UserSummary(
                "username"+postfixNumber,
                "name"+postfixNumber,
                "email"+postfixNumber,
                "gravatarUrl"+postfixNumber);
    }
}
