package com.list.todo.it;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.controllers.UserController;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import com.list.todo.payload.*;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.UserService;
import com.list.todo.services.UserSettingsService;
import com.list.todo.services.UserStatisticsService;
import com.list.todo.util.IdComparator;
import com.list.todo.util.PageableStub;
import com.list.todo.util.UserSummaryComparator;
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

import static com.list.todo.util.ObjectsProvider.createUserStatistics;
import static com.list.todo.util.ObjectsProvider.createUserSummary;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final String USERNAME = "anna";
    private static final String PART_OF_USERNAME = "an";

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserSettingsService settingsService;

    @Mock
    private FollowerService followerService;

    @Mock
    private UserStatisticsService userStatisticsService;

    private PageableStub pageable = new PageableStub();

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserSummaryComparator userSummaryComparator = new UserSummaryComparator();


    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(CURRENT_USER_ID);
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
        UserSummary userSummary = createUserSummary(1);
        when(userService.getUserInfo(any(UserPrincipal.class))).thenReturn(userSummary);

        //act, assert
        this.mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(jsonPath("name").value(userSummary.getName()))
                .andExpect(jsonPath("username").value(userSummary.getUsername()))
                .andExpect(jsonPath("email").value(userSummary.getEmail()))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserSettings_OnExistentUser_ReturnsAnObjectOfUserSettings() throws Exception {
        //arrange
        UserSettings userSettings = new UserSettings(true, true);
        when(settingsService.getUserSettingsByUserId(CURRENT_USER_ID)).thenReturn(Optional.of(userSettings));

        //act, assert
        this.mockMvc.perform(get("/api/users/settings"))
                .andDo(print())
                .andExpect(jsonPath("isEnableEmailNotification").value(userSettings.getIsEnableEmailNotification()))
                .andExpect(jsonPath("isEnableWebSocketNotification").value(userSettings.getIsEnableWebSocketNotification()))
                .andExpect(status().isOk());
        verify(settingsService).getUserSettingsByUserId(CURRENT_USER_ID);
    }

    @Test
    public void updateUserSettings_OnExistentUser_ReturnsAnObjectOfUserSettings() throws Exception {
        //arrange
        UserSettings userSettings = new UserSettings(true, true);
        UserSettingsInput userSettingsInput = new UserSettingsInput(
                userSettings.getIsEnableEmailNotification(),
                userSettings.getIsEnableWebSocketNotification()
        );
        when(settingsService.updateUserSettings(userSettingsInput, CURRENT_USER_ID)).thenReturn(Optional.of(userSettings));

        //act, assert
        this.mockMvc.perform(put("/api/users/settings")
                .content(objectMapper.writeValueAsString(userSettingsInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("isEnableEmailNotification").value(userSettings.getIsEnableEmailNotification()))
                .andExpect(jsonPath("isEnableWebSocketNotification").value(userSettings.getIsEnableWebSocketNotification()))
                .andExpect(status().isOk());
        verify(settingsService).updateUserSettings(userSettingsInput, CURRENT_USER_ID);
    }

    @Test
    public void searchUsersByUsername_OnExistentUser_ReturnsASetOfStrings() throws Exception {
        //arrange
        Set usernames = new HashSet();
        usernames.add(USERNAME);
        when(userService.searchUsersByPartOfUsername(PART_OF_USERNAME)).thenReturn(usernames);

        //act, assert
        this.mockMvc.perform(get("/api/users/search?username={username}", PART_OF_USERNAME))
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().json("[\"anna\"]"))
                .andExpect(status().isOk());

    }

    @Test
    public void searchUsersByNonExistentUsername_ReturnsAnEmptySet() throws Exception {
        //arrange
        Set usernames = new HashSet();
        when(userService.searchUsersByPartOfUsername(PART_OF_USERNAME)).thenReturn(usernames);

        //act, assert
        this.mockMvc.perform(get("/api/users/search?username={username}", PART_OF_USERNAME))
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

        //assert
        assertEqualsTodoLists(todoLists1, returnedMyTodoLists);
        assertEqualsTodoLists(todoLists2, returnedSharedTodoLists);
    }

    @Test
    public void updateUser_OnExistentUser_ReturnsAnObjectOfUser() throws Exception {
        //arrange
        UpdatingUserInput userInput = new UpdatingUserInput();
        userInput.setName("name");
        userInput.setUsername("username");
        userInput.setEmail("email@example.ua");
        userInput.setPassword("password");
        when(userService.updateUser(CURRENT_USER_ID, userInput)).thenReturn(Optional.of(new User(
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

        verify(userService).updateUser(CURRENT_USER_ID, userInput);
    }

    @Test
    public void deleteUser_OnExistentUser_ReturnsStatusNoContent() throws Exception {
        //act
        this.mockMvc.perform(delete("/api/users/deleteProfile"))
                .andDo(print())
                .andExpect(status().isNoContent());

        //assert
        verify(userService).deleteUser(CURRENT_USER_ID);
    }

    @Test
    public void followUser_OnExistentUser_ReturnsStatusOk() throws Exception {
        //arrange
        when(followerService.isAlreadyFollowed(CURRENT_USER_ID, USERNAME)).thenReturn(false);
        when(followerService.followUser(CURRENT_USER_ID, USERNAME)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", USERNAME))
                .andDo(print())
                .andExpect(status().isOk());

        //assert
        verify(followerService).isAlreadyFollowed(CURRENT_USER_ID, USERNAME);
        verify(followerService).followUser(CURRENT_USER_ID, USERNAME);
    }

    @Test
    public void follow_OnNonExistentUser_ReturnsStatusNotFound() throws Exception {
        //arrange
        when(followerService.isAlreadyFollowed(CURRENT_USER_ID, USERNAME)).thenReturn(false);
        when(followerService.followUser(CURRENT_USER_ID, USERNAME)).thenReturn(false);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", USERNAME))
                .andDo(print())
                .andExpect(status().isNotFound());

        //assert
        verify(followerService).isAlreadyFollowed(CURRENT_USER_ID, USERNAME);
        verify(followerService).followUser(CURRENT_USER_ID, USERNAME);
    }

    @Test
    public void followAlreadyFollowedUser_ReturnsStatusIsForbidden() throws Exception {
        //arrange
        when(followerService.isAlreadyFollowed(CURRENT_USER_ID, USERNAME)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/followUser?username={username}", USERNAME))
                .andDo(print())
                .andExpect(status().isForbidden());

        //assert
        verify(followerService).isAlreadyFollowed(CURRENT_USER_ID, USERNAME);
        verify(followerService, times(0)).followUser(CURRENT_USER_ID, USERNAME);
    }

    @Test
    public void unfollowUser_OnExistentUser_ReturnsStatusOk() throws Exception {
        //arrange
        when(followerService.isAlreadyFollowed(CURRENT_USER_ID, USERNAME)).thenReturn(true);
        when(followerService.unfollowUser(CURRENT_USER_ID, USERNAME)).thenReturn(true);

        //act
        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", USERNAME))
                .andDo(print())
                .andExpect(status().isOk());

        //assert
        verify(followerService).isAlreadyFollowed(CURRENT_USER_ID, USERNAME);
        verify(followerService).unfollowUser(CURRENT_USER_ID, USERNAME);
    }

    @Test
    public void unfollow_OnNonExistentUser_ReturnsStatusIsForbidden() throws Exception {
        //arrange
        when(followerService.isAlreadyFollowed(CURRENT_USER_ID, USERNAME)).thenReturn(false);

        //act
        this.mockMvc.perform(post("/api/users/unfollowUser?username={username}", USERNAME))
                .andDo(print())
                .andExpect(status().isForbidden());

        //assert
        verify(followerService).isAlreadyFollowed(CURRENT_USER_ID, USERNAME);
        verify(followerService, times(0)).unfollowUser(CURRENT_USER_ID, USERNAME);
    }

    @Test
    public void getFollowers_OnExistentUser_ReturnsAListOfUserSummaries() throws Exception {
        //arrange
        List<UserSummary> userSummaries = new ArrayList<>();
        UserSummary userSummary1 = createUserSummary(1);
        UserSummary userSummary2 = createUserSummary(2);
        userSummaries.add(userSummary1);
        userSummaries.add(userSummary2);

        when(followerService.getFollowersUserSummariesByUserId(CURRENT_USER_ID)).thenReturn(userSummaries);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/followers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<UserSummary> returnedUserSummaries = getUserSummariesFromJsonResponse(result.getResponse().getContentAsString());
        userSummaries.sort(userSummaryComparator);
        returnedUserSummaries.sort(userSummaryComparator);

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

        when(followerService.getFollowedUserSummariesByUserId(CURRENT_USER_ID)).thenReturn(userSummaries);

        //act
        MvcResult result = this.mockMvc.perform(get("/api/users/followed"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<UserSummary> returnedUserSummaries = getUserSummariesFromJsonResponse(result.getResponse().getContentAsString());
        userSummaries.sort(userSummaryComparator);
        returnedUserSummaries.sort(userSummaryComparator);

        //assert
        Assert.assertEquals(userSummaries, returnedUserSummaries);
    }

    @Test
    public void getUserStatistics_OnExistentUser_ReturnsAnObjectOfUserStatistics() throws Exception {
        //arrange
        UserStatistics userStatistics = createUserStatistics();

        when(userStatisticsService.getUserStatisticsByUserId(CURRENT_USER_ID)).thenReturn(userStatistics);

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
            todoList.setTodoListName("name" + i);
            todoList.setCreatedBy(createdBy);
            todoList.setId(i);

            todoLists.add(todoList);
        }

        return todoLists;
    }

    private List<TodoList> getTodoListsFromJsonResponse(String response, String arrayName) throws JSONException, IOException {
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


    private void assertEqualsTodoLists(List<TodoList> todoLists1, List<TodoList> todoLists2) {
        IdComparator idComparator = new IdComparator();
        todoLists1.sort(idComparator);
        todoLists2.sort(idComparator);

        Assert.assertEquals(todoLists1, todoLists2);
    }
}
