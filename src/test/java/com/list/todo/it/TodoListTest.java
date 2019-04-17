package com.list.todo.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.controllers.TodoListController;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.entity.User;
import com.list.todo.payload.TodoListInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import com.list.todo.util.IdComparator;
import com.list.todo.util.JsonParser;
import com.list.todo.util.ObjectsProvider;
import com.sun.xml.internal.bind.v2.TODO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TodoListTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private TodoListController todoListController;

    @Mock
    private TodoListService todoListService;

    @Mock
    private ShareService shareService;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonParser<TodoList> jsonParser = new JsonParser<>(TodoList.class);

    private IdComparator idComparator = new IdComparator();

    private static final Long USER_ID = 1L;
    private static final String USER_USERNAME = "username";
    private static final Long SECOND_USER_ID = 2L;
    private static final String SECOND_USER_USERNAME = "username2";
    private static final Long TODO_LIST_ID = 3L;
    private static final String TODO_LIST_NAME = "todoListName";
    private static final String TODO_LIST_COMMENT = "comment";

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(USER_ID);
            userPrincipal.setUsername(USER_USERNAME);
            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("root"));

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
                .standaloneSetup(todoListController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
                .build();
    }

    @Test
    public void getTodoLists_getExistentTodoLists_OkStatus() throws Exception {
        List<TodoList> todoLists = ObjectsProvider.createListOfTodoLists();

        when(todoListService.getTodoListsByUser(any(UserPrincipal.class), eq(TodoListStatus.ACTIVE), any(Pageable.class), eq(new ArrayList<>())))
                .thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists?status={status}&tagId=", "ACTIVE")
                .with(user(USER_USERNAME))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());

        todoLists.sort(idComparator);
        todoListsFromJsonResponse.sort(idComparator);

        Assert.assertEquals(todoLists, todoListsFromJsonResponse);
    }

    @Test
    public void getMySharedTodoLists_getExistentTodoLists_OkStatus() throws Exception {

        List<TodoList> todoLists = ObjectsProvider.createListOfTodoLists();

        when(shareService.getSharedTodoListsByUser(USER_ID)).thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists/shared")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());

        todoLists.sort(idComparator);
        todoListsFromJsonResponse.sort(idComparator);

        Assert.assertEquals(todoLists, todoListsFromJsonResponse);
    }

    @Test
    public void getTodoList_getExistentTodoList_OkStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
                .andExpect(jsonPath("comment").value(TODO_LIST_COMMENT))
                .andExpect(jsonPath("todoListStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.tasks[0].body").value("task"))
                .andExpect(jsonPath("$.tasks[0].isComplete").value("false"))
                .andExpect(jsonPath("$.tasks[0].priority").value("NOT_SPECIFIED"))
                .andReturn();

         verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);

    }

    @Test
    public void getTodoList_getNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTodoList_getTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setCreatedBy(SECOND_USER_USERNAME);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void addTodoList_addCorrectTodoList_OkStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        TodoListInput todoListInput = new TodoListInput(todoList.getTodoListName(), todoList.getComment(), todoList.getTasks());

        when(todoListService.addTodoList(todoListInput, USER_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(post("/api/todolists").content(objectMapper.writeValueAsString(todoListInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        verify(todoListService, times(1)).addTodoList(todoListInput, USER_ID);
    }

    @Test
    public void updateTodoList_updateExistentTodoLists_OkStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        TodoList updatedTodoList = ObjectsProvider.createTodoList();
        updatedTodoList.setId(TODO_LIST_ID);
        updatedTodoList.setTodoListName(TODO_LIST_NAME);
        updatedTodoList.setComment(TODO_LIST_COMMENT);

        TodoListInput todoListInput = new TodoListInput(TODO_LIST_NAME, TODO_LIST_COMMENT, ObjectsProvider.createSetOfTasks());

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(todoListService.updateTodoList(TODO_LIST_ID, todoListInput, USER_ID)).thenReturn(Optional.of(updatedTodoList));

        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID)
                .content(objectMapper.writeValueAsString(todoListInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
                .andExpect(jsonPath("$.tasks[0].body").value("task"))
                .andExpect(jsonPath("$.tasks[0].isComplete").value("false"))
                .andExpect(jsonPath("$.tasks[0].priority").value("NOT_SPECIFIED"))
                .andExpect(status().isOk());

        verify(todoListService).updateTodoList(TODO_LIST_ID, todoListInput, USER_ID);
    }

    @Test
    public void updateTodoList_updateNonExistentTodoList_NotFoundStatus() throws Exception {

        TodoListInput todoListInput = new TodoListInput();

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID).content(objectMapper.writeValueAsString(todoListInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTodoList_updateTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setCreatedBy(SECOND_USER_USERNAME);

        TodoListInput todoListInput = new TodoListInput();

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID).content(objectMapper.writeValueAsString(todoListInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void enableTodoList_enableExistentTodolist_OkStatus() throws Exception {
        Optional<TodoList> todoList = Optional.of(ObjectsProvider.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
        Optional<TodoList> enabledTodoList = Optional.of(ObjectsProvider.createTodoList());
        enabledTodoList.get().setTodoListStatus(TodoListStatus.ACTIVE);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
        when(todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE)).thenReturn(enabledTodoList);

        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
                .andExpect(status().isOk());
    }

    @Test
    public void enableTodoList_enableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
        Optional<TodoList> todoList = Optional.of(ObjectsProvider.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
        todoList.get().setCreatedBy(SECOND_USER_USERNAME);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);

        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE);

    }

    @Test
    public void enableTodoList_enableNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE);
    }

    @Test
    public void disableTodoList_disableExistentTodolist_OkStatus() throws Exception {
        Optional<TodoList> todoList = Optional.of(ObjectsProvider.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
        Optional<TodoList> disabledTodoList = Optional.of(ObjectsProvider.createTodoList());
        disabledTodoList.get().setTodoListStatus(TodoListStatus.INACTIVE);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
        when(todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE)).thenReturn(disabledTodoList);

        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
                .andExpect(status().isOk());
    }

    @Test
    public void disableTodoList_disableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
        Optional<TodoList> todoList = Optional.of(ObjectsProvider.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
        todoList.get().setCreatedBy(SECOND_USER_USERNAME);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);

        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);

    }

    @Test
    public void disableTodoList_disableNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);
    }

    @Test
    public void deleteTodoList_deleteExistentTodoList_OkStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(delete("/api/todolists/{todoListId}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(todoListService).deleteTodoList(TODO_LIST_ID, USER_ID);
    }

    @Test
    public void deleteTodoList_deleteNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/todolists/{todoListId}", TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).deleteTodoList(TODO_LIST_ID, USER_ID);

    }

    @Test
    public void deleteTodoList_deleteTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setCreatedBy(SECOND_USER_USERNAME);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(delete("/api/todolists/{todoListId}",  TODO_LIST_ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).deleteTodoList(TODO_LIST_ID, USER_ID);

    }

    @Test
    public void shareTodoListToUser_shareExistentTodoListToUser_OkStatus() throws Exception {

        User user = new User();
        user.setId(USER_ID);

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk());

        verify(todoListService).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
    }

    @Test
    public void shareTodoListToUser_shareNonExistentTodoListToUser_NotFoundStatus() throws Exception {

        User user = new User();
        user.setId(USER_ID);

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);

    }

    @Test
    public void shareTodoListToUser_shareTodoListToNonExistentUser_NotFoundStatus() throws Exception {

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.empty());
        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
    }

    @Test
    public void shareTodoListToUser_shareMyTodoListToMyself_ForbiddenStatus() throws Exception {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);
        todoList.setCreatedBy(USER_USERNAME);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(todoList, USER_ID)).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).shareTodoList(USER_USERNAME, TODO_LIST_ID, USER_ID);
    }

    @Test
    public void shareTodoListToUser_shareTodoListToMyself_ForbiddenStatus() throws Exception {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(todoList, USER_ID)).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).shareTodoList(USER_USERNAME, TODO_LIST_ID, USER_ID);
    }

    @Test
    public void shareTodoListToUser_shareTodoListToAnotherUserMoreThanOneTime_ConflictStatus() throws Exception {
        User user = new User();
        user.setId(SECOND_USER_ID);

        TodoList todoList = ObjectsProvider.createTodoList();
        todoList.setId(TODO_LIST_ID);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(true);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
    }

    @Test
    public void getTodoListsByName() throws Exception {
        List<TodoList> todoLists = ObjectsProvider.createListOfTodoLists();

        when(todoListService.searchTodoListByName(eq(TODO_LIST_NAME + "%"), eq(USER_USERNAME), any(Pageable.class)))
                .thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists/search?name={todoListName}", TODO_LIST_NAME)
                .with(user(USER_USERNAME))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());

        todoLists.sort(idComparator);
        todoListsFromJsonResponse.sort(idComparator);

        Assert.assertEquals(todoLists, todoListsFromJsonResponse);
    }
}
