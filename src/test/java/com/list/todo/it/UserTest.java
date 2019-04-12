package com.list.todo.it;

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

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(1L);
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
    public void beforeMethod() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
                .build();
    }


    @Test
    public void userInfo() throws Exception {
        when(userService.getUserInfo(any(UserPrincipal.class))).thenReturn(new UserSummary("stepanich", "Stepan Matveev", "stepa.matv72@gmail.com", null));

        this.mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(jsonPath("name").value("Stepan Matveev"))
                .andExpect(jsonPath("username").value("stepanich"))
                .andExpect(jsonPath("email").value("stepa.matv72@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    public void serchUsersByUsername() throws Exception {
        Set usernames = new HashSet();
        usernames.add("anna");
        when(userService.searchUsersByPartOfUsername(anyString())).thenReturn(usernames);

        this.mockMvc.perform(get("/api/users/search?username={username}", "ann"))
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().json("[\"anna\"]"))
                .andExpect(status().isOk());

    }

    @Test
    public void serchUsersByNonExistentUsername() throws Exception {
        Set usernames = new HashSet();
        when(userService.searchUsersByPartOfUsername(anyString())).thenReturn(usernames);

        this.mockMvc.perform(get("/api/users/search?username={username}", "kkkkk"))
                .andDo(print())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(status().isOk());
    }

    @Test
    public void getUserStats() throws Exception {
        int countOfTodoLists = 2;
        List<TodoList> todoLists1 = this.createListOfTodoLists(countOfTodoLists, "username1");
        List<TodoList> todoLists2 = this.createListOfTodoLists(countOfTodoLists, "username2");
        Page<TodoList> myTodoLists = new PageImpl<>(todoLists1, pageable, todoLists1.size());
        Page<TodoList> sharedTodoLists = new PageImpl<>(todoLists2, pageable, todoLists2.size());
        UserStats userStats = new UserStats(myTodoLists, sharedTodoLists);
        when(userService.getUserStats(any(UserPrincipal.class), any(Pageable.class))).thenReturn(userStats);

        MvcResult result = this.mockMvc.perform(get("/api/users/userStats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> returnedMyTodoLists = getTodoListsFromJsonResponse(
                result.getResponse().getContentAsString(),
                "myTodoLists",
                todoLists1.size());
        List<TodoList> returnedSharedTodoLists = getTodoListsFromJsonResponse(
                result.getResponse().getContentAsString(),
                "sharedTodoLists",
                todoLists1.size());

        Assert.assertEquals(todoLists1.hashCode(), returnedMyTodoLists.hashCode());
        Assert.assertEquals(todoLists2.hashCode(), returnedSharedTodoLists.hashCode());
    }

    @Test
    public void updateUser() throws Exception {
        UpdatingUserInput userInput = new UpdatingUserInput();
        userInput.setName("Stepa Baklagan");
        userInput.setUsername("stepka");
        userInput.setEmail("stepa.matv72@gmail.com");
        userInput.setPassword("Secrett");
        when(userService.updateUser(1L, userInput)).thenReturn(Optional.of(new User(
                userInput.getName(),
                userInput.getUsername(),
                userInput.getEmail(),
                userInput.getPassword(),
                "gravatarHash")));

        this.mockMvc.perform(put("/api/users/editProfile").content("{\n" +
                "	\"name\": \"Stepa Baklagan\",\n" +
                "	\"username\": \"stepka\",\n" +
                "	\"email\": \"stepa.matv72@gmail.com\",\n" +
                "	\"password\": \"Secrett\"\n" +
                "}").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(jsonPath("name").value("Stepa Baklagan"))
                .andExpect(jsonPath("username").value("stepka"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUser() throws Exception {
        this.mockMvc.perform(delete("/api/users/deleteProfile"))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(1L);
    }

    @Test
    public void followUser() throws Exception {
        String followedUsername = "anna";
        when(followerService.isAlreadyFollowed(1L, followedUsername)).thenReturn(false);
        when(followerService.followUser(1L, followedUsername)).thenReturn(true);
        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isOk());
        verify(followerService).isAlreadyFollowed(1L, followedUsername);
        verify(followerService).followUser(1L, followedUsername);
    }

    @Test
    public void followNonExistentUser() throws Exception {
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(1L, followedUsername)).thenReturn(false);
        when(followerService.followUser(1L, followedUsername)).thenReturn(false);

        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(followerService).isAlreadyFollowed(1L, followedUsername);
        verify(followerService).followUser(1L, followedUsername);
    }

    @Test
    public void followAlreadyFollowedUser() throws Exception {
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(1L, followedUsername)).thenReturn(true);

        this.mockMvc.perform(post("/api/users/followUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(followerService).isAlreadyFollowed(1L, followedUsername);
        verify(followerService, times(0)).followUser(1L, followedUsername);
    }

    @Test
    public void unfollowUser() throws Exception {
        String followedUsername = "anna";
        when(followerService.isAlreadyFollowed(1L, followedUsername)).thenReturn(true);
        when(followerService.unfollowUser(1L, followedUsername)).thenReturn(true);

        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isOk());

        verify(followerService).isAlreadyFollowed(1L, followedUsername);
        verify(followerService).unfollowUser(1L, followedUsername);
    }

    @Test
    public void unfollowNonExistentUser() throws Exception {
        String followedUsername = "annaanna";
        when(followerService.isAlreadyFollowed(1L, followedUsername)).thenReturn(false);
        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", followedUsername))
                .andDo(print())
                .andExpect(status().isForbidden());
        verify(followerService).isAlreadyFollowed(1L, followedUsername);
        verify(followerService, times(0)).unfollowUser(1L, followedUsername);
    }

    @Test
    public void getFollowers() throws Exception {
        List<UserSummary> userSummaries = new ArrayList<>();
        UserSummary userSummary1 = new UserSummary(
                "username1", "name1", "email1", "gravatarUrl");
        UserSummary userSummary2 = new UserSummary(
                "username2", "name2", "email2", "gravatarUrl");
        userSummaries.add(userSummary1);
        userSummaries.add(userSummary2);

        when(followerService.getFollowersUserSummariesByUserId(1L)).thenReturn(userSummaries);
        MvcResult result = this.mockMvc.perform(get("/api/users/followers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray jsonArray = new JSONArray(result.getResponse().getContentAsString());

        for (int i=0; i<userSummaries.size(); i++){
            UserSummary returnedUserSummary = objectMapper
                    .readValue(jsonArray.get(i).toString(), UserSummary.class);
            Assert.assertTrue(userSummaries.contains(returnedUserSummary));
        }
    }

    @Test
    public void getFollowedUsers() throws Exception {
        List<UserSummary> userSummaries = new ArrayList<>();
        UserSummary userSummary1 = new UserSummary(
                "username1", "name1", "email1", "gravatarUrl");
        UserSummary userSummary2 = new UserSummary(
                "username2", "name2", "email2", "gravatarUrl");
        userSummaries.add(userSummary1);
        userSummaries.add(userSummary2);

        when(followerService.getFollowedUserSummariesByUserId(1L)).thenReturn(userSummaries);
        MvcResult result = this.mockMvc.perform(get("/api/users/followed"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray jsonArray = new JSONArray(result.getResponse().getContentAsString());

        for (int i=0; i<userSummaries.size(); i++){
            UserSummary returnedUserSummary = objectMapper
                    .readValue(jsonArray.get(i).toString(), UserSummary.class);
            Assert.assertTrue(userSummaries.contains(returnedUserSummary));
        }
    }

    @Test
    public void getUserStatistics() throws Exception {
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setTodoListsNumber(21L);
        userStatistics.setTasksNumber(11L);
        userStatistics.setCompletedTasksNumber(5L);
        userStatistics.setFollowedUsersNumber(2);
        userStatistics.setFollowersNumber(3);

        when(userStatisticsService.getUserStatisticsByUserId(1L)).thenReturn(userStatistics);

        MvcResult result = this.mockMvc.perform(get("/api/users/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        UserStatistics returnedUserStatistics = objectMapper
                .readValue(result.getResponse().getContentAsString(), UserStatistics.class);

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

    private List<TodoList> getTodoListsFromJsonResponse(String response,String arrayName, int numberOfTodoLists) throws JSONException, IOException {
        List<TodoList> returnedTodoLists = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONObject myTodoListsPageJson = (JSONObject) jsonObject.get(arrayName);
        JSONArray myTodoListsJson = myTodoListsPageJson.getJSONArray("content");
        ObjectMapper objectMapper = new ObjectMapper();
        for (int i=0; i<numberOfTodoLists; i++){
            TodoList returnedTodoList = objectMapper
                    .readValue(myTodoListsJson.get(i).toString(), TodoList.class);
            returnedTodoLists.add(returnedTodoList);
        }
        return returnedTodoLists;
    }
}
