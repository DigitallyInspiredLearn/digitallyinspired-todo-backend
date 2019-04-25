//package com.list.todo.it;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.list.todo.TodoListApplication;
//import com.list.todo.configurations.H2TestProfileJPAConfig;
//import com.list.todo.controllers.TodoListController;
//import com.list.todo.entity.TodoList;
//import com.list.todo.entity.TodoListStatus;
//import com.list.todo.entity.User;
//import com.list.todo.payload.TodoListInput;
//import com.list.todo.security.UserPrincipal;
//import com.list.todo.services.ShareService;
//import com.list.todo.services.TodoListService;
//import com.list.todo.services.UserService;
//import com.list.todo.util.IdComparator;
//import com.list.todo.util.JsonParser;
//import com.list.todo.util.ObjectsProvider;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.MethodParameter;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static com.list.todo.util.ObjectsProvider.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = {TodoListApplication.class, H2TestProfileJPAConfig.class})
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//public class TodoListTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @InjectMocks
//    private TodoListController todoListController;
//
//    @Mock
//    private TodoListService todoListService;
//
//    @Mock
//    private ShareService shareService;
//
//    @Mock
//    private UserService userService;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    private JsonParser<TodoList> jsonParser = new JsonParser<>(TodoList.class);
//
//    private static final Long USER_ID = 1L;
//    private static final String USER_USERNAME = "username";
//    private static final Long SECOND_USER_ID = 2L;
//    private static final String SECOND_USER_USERNAME = "username2";
//    private static final Long TODO_LIST_ID = 3L;
//    private static final String TODO_LIST_NAME = "todoListName";
//    private static final String TODO_LIST_COMMENT = "comment";
//
//    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
//        @Override
//        public boolean supportsParameter(MethodParameter parameter) {
//            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
//        }
//
//        @Override
//        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
//                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//            UserPrincipal userPrincipal = new UserPrincipal();
//            userPrincipal.setId(USER_ID);
//            userPrincipal.setUsername(USER_USERNAME);
//            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("root"));
//
//            return userPrincipal;
//        }
//    };
//
//    private HandlerMethodArgumentResolver putPageable = new HandlerMethodArgumentResolver() {
//
//        @Override
//        public boolean supportsParameter(
//                MethodParameter parameter) {
//            if (parameter.getParameterType().equals(
//                    Pageable.class)) {
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public Object resolveArgument(
//                MethodParameter parameter,
//                ModelAndViewContainer mavContainer,
//                NativeWebRequest webRequest,
//                WebDataBinderFactory binderFactory) throws Exception {
//
//            return new PageRequest(0, 50);
//        }
//    };
//
//    @Before
//    public void before() {
//        mockMvc = MockMvcBuilders
//                .standaloneSetup(todoListController)
//                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
//                .build();
//    }
//
//    @Test
//    public void getTodoLists_getExistingTodoLists_OkStatus() throws Exception {
//        // arrange
//        List<TodoList> todoLists = createListOfTodoLists();
//
//        when(todoListService.getTodoListsByUser(any(UserPrincipal.class), eq(TodoListStatus.ACTIVE), any(Pageable.class), eq(new ArrayList<>())))
//                .thenReturn(todoLists);
//
//        // act
//        MvcResult result = this.mockMvc.perform(get("/api/todolists?status={status}&tagId=", TodoListStatus.ACTIVE)
//                .with(user(USER_USERNAME))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListsByUser(any(UserPrincipal.class), eq(TodoListStatus.ACTIVE), any(Pageable.class), eq(new ArrayList<>()));
//        this.assertListsEqual(todoLists, todoListsFromJsonResponse);
//    }
//
//    @Test
//    public void getMySharedTodoLists_getExistingTodoLists_OkStatus() throws Exception {
//
//        // arrange
//        List<TodoList> todoLists = createListOfTodoLists();
//
//        when(shareService.getSharedTodoListsByUser(eq(USER_ID), any(Pageable.class))).thenReturn(todoLists);
//
//        // act
//        MvcResult result = this.mockMvc.perform(get("/api/todolists/shared")
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());
//
//        // assert
//        verify(shareService, times(1)).getSharedTodoListsByUser(eq(USER_ID), any(Pageable.class));
//        this.assertListsEqual(todoLists, todoListsFromJsonResponse);
//    }
//
//    @Test
//    public void getTodoList_getExistingTodoList_OkStatus() throws Exception {
//
//        // arrange
//        TodoList todoList = createTodoList();
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
//                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
//                .andExpect(jsonPath("comment").value(TODO_LIST_COMMENT))
//                .andExpect(jsonPath("todoListStatus").value("ACTIVE"))
//                .andExpect(jsonPath("$.tasks[0].body").value("task"))
//                .andExpect(jsonPath("$.tasks[0].isComplete").value("false"))
//                .andExpect(jsonPath("$.tasks[0].priority").value("NOT_SPECIFIED"))
//                .andReturn();
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//
//    }
//
//    @Test
//    public void getTodoList_getNonExistingTodoList_NotFoundStatus() throws Exception {
//
//        // arrange
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//
//        // act
//        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService).getTodoListById(TODO_LIST_ID);
//    }
//
//    @Test
//    public void getTodoList_getTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
//
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setCreatedBy(SECOND_USER_USERNAME);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(get("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//    }
//
//    @Test
//    public void addTodoList_addCorrectTodoList_OkStatus() throws Exception {
//
//        // arrange
//        TodoList todoList = createTodoList();
//        TodoListInput todoListInput = new TodoListInput(todoList.getTodoListName(), todoList.getComment(), todoList.getTasks());
//
//        when(todoListService.addTodoList(todoListInput, USER_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(post("/api/todolists").content(objectMapper.writeValueAsString(todoListInput))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk()).andReturn();
//
//        // assert
//        verify(todoListService, times(1)).addTodoList(todoListInput, USER_ID);
//    }
//
//    @Test
//    public void updateTodoList_updateExistingTodoLists_OkStatus() throws Exception {
//
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        TodoList updatedTodoList = createTodoList();
//        updatedTodoList.setId(TODO_LIST_ID);
//        updatedTodoList.setTodoListName(TODO_LIST_NAME);
//        updatedTodoList.setComment(TODO_LIST_COMMENT);
//
//        TodoListInput todoListInput = new TodoListInput(TODO_LIST_NAME, TODO_LIST_COMMENT, createSetOfTasks());
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(todoListService.updateTodoList(TODO_LIST_ID, todoListInput, USER_ID)).thenReturn(Optional.of(updatedTodoList));
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .content(objectMapper.writeValueAsString(todoListInput))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
//                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
//                .andExpect(jsonPath("$.tasks[0].body").value("task"))
//                .andExpect(jsonPath("$.tasks[0].isComplete").value("false"))
//                .andExpect(jsonPath("$.tasks[0].priority").value("NOT_SPECIFIED"))
//                .andExpect(status().isOk());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, times(1)).updateTodoList(TODO_LIST_ID, todoListInput, USER_ID);
//    }
//
//    @Test
//    public void updateTodoList_updateNonExistingTodoList_NotFoundStatus() throws Exception {
//
//        // arrange
//        TodoListInput todoListInput = new TodoListInput();
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID).content(objectMapper.writeValueAsString(todoListInput))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//    }
//
//    @Test
//    public void updateTodoList_updateTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
//
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setCreatedBy(SECOND_USER_USERNAME);
//
//        TodoListInput todoListInput = new TodoListInput();
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/{todoListId}", TODO_LIST_ID).content(objectMapper.writeValueAsString(todoListInput))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//    }
//
//    @Test
//    public void enableTodoList_enableExistingTodolist_OkStatus() throws Exception {
//        // arrange
//        Optional<TodoList> todoList = Optional.of(createTodoList());
//        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
//        Optional<TodoList> enabledTodoList = Optional.of(createTodoList());
//        enabledTodoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
//        when(todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE)).thenReturn(enabledTodoList);
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
//                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
//                .andExpect(status().isOk());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, times(1)).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE);
//    }
//
//    @Test
//    public void enableTodoList_enableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
//        // arrange
//        Optional<TodoList> todoList = Optional.of(createTodoList());
//        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
//        todoList.get().setCreatedBy(SECOND_USER_USERNAME);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE);
//
//    }
//
//    @Test
//    public void enableTodoList_enableNonExistingTodoList_NotFoundStatus() throws Exception {
//        // arrange
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/enable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.ACTIVE);
//    }
//
//    @Test
//    public void disableTodoList_disableExistingTodolist_OkStatus() throws Exception {
//        // arrange
//        Optional<TodoList> todoList = Optional.of(createTodoList());
//        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
//        Optional<TodoList> disabledTodoList = Optional.of(createTodoList());
//        disabledTodoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
//        when(todoListService.changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE)).thenReturn(disabledTodoList);
//
//        // act, assert
//        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(jsonPath("todoListName").value(TODO_LIST_NAME))
//                .andExpect(jsonPath("createdBy").value(USER_USERNAME))
//                .andExpect(status().isOk());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, times(1)).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);
//    }
//
//    @Test
//    public void disableTodoList_disableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
//        // arrange
//        Optional<TodoList> todoList = Optional.of(createTodoList());
//        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
//        todoList.get().setCreatedBy(SECOND_USER_USERNAME);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);
//
//    }
//
//    @Test
//    public void disableTodoList_disableNonExistingTodoList_NotFoundStatus() throws Exception {
//        // arrange
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//
//        // act
//        this.mockMvc.perform(put("/api/todolists/disable/{id}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).changeTodoListStatus(TODO_LIST_ID, TodoListStatus.INACTIVE);
//    }
//
//    @Test
//    public void deleteTodoList_deleteExistingTodoList_OkStatus() throws Exception {
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(delete("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNoContent());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService).deleteTodoList(TODO_LIST_ID, USER_ID);
//    }
//
//    @Test
//    public void deleteTodoList_deleteNonExistingTodoList_NotFoundStatus() throws Exception {
//        // arrange
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//
//        // act
//        this.mockMvc.perform(delete("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, never()).deleteTodoList(TODO_LIST_ID, USER_ID);
//
//    }
//
//    @Test
//    public void deleteTodoList_deleteTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setCreatedBy(SECOND_USER_USERNAME);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//
//        // act
//        this.mockMvc.perform(delete("/api/todolists/{todoListId}", TODO_LIST_ID)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).deleteTodoList(TODO_LIST_ID, USER_ID);
//
//    }
//
//    @Test
//    public void shareTodoListToUser_shareExistingTodoListToUser_OkStatus() throws Exception {
//        // arrange
//        User user = new User();
//        user.setId(USER_ID);
//
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
//        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(userService).getUserByUsername(SECOND_USER_USERNAME);
//        verify(todoListService).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
//    }
//
//    @Test
//    public void shareTodoListToUser_shareNonExistingTodoListToUser_NotFoundStatus() throws Exception {
//        // arrange
//        User user = new User();
//        user.setId(USER_ID);
//
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());
//        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
//        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
//        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
//
//
//    }
//
//    @Test
//    public void shareTodoListToUser_shareTodoListToNonExistingUser_NotFoundStatus() throws Exception {
//        // arrange
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.empty());
//        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(false);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
//        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
//    }
//
//    @Test
//    public void shareTodoListToUser_shareMyTodoListToMyself_ForbiddenStatus() throws Exception {
//        // arrange
//        User user = new User();
//        user.setId(USER_ID);
//        user.setUsername(USER_USERNAME);
//
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//        todoList.setCreatedBy(USER_USERNAME);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(user));
//        when(shareService.isSharedTodoListToUser(todoList, USER_ID)).thenReturn(false);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).shareTodoList(USER_USERNAME, TODO_LIST_ID, USER_ID);
//        verify(userService, times(1)).getUserByUsername(USER_USERNAME);
//    }
//
//    @Test
//    public void shareTodoListToUser_shareTodoListToMyself_ForbiddenStatus() throws Exception {
//        // arrange
//        User user = new User();
//        user.setId(USER_ID);
//        user.setUsername(USER_USERNAME);
//
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(USER_USERNAME)).thenReturn(Optional.of(user));
//        when(shareService.isSharedTodoListToUser(todoList, USER_ID)).thenReturn(false);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).shareTodoList(USER_USERNAME, TODO_LIST_ID, USER_ID);
//        verify(userService, times(1)).getUserByUsername(USER_USERNAME);
//    }
//
//    @Test
//    public void shareTodoListToUser_shareTodoListToAnotherUserMoreThanOneTime_ConflictStatus() throws Exception {
//        // arrange
//        User user = new User();
//        user.setId(SECOND_USER_ID);
//
//        TodoList todoList = createTodoList();
//        todoList.setId(TODO_LIST_ID);
//
//        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(SECOND_USER_USERNAME)).thenReturn(Optional.of(user));
//        when(shareService.isSharedTodoListToUser(todoList, SECOND_USER_ID)).thenReturn(true);
//
//        // act
//        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", TODO_LIST_ID, SECOND_USER_USERNAME)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isConflict());
//
//        // assert
//        verify(todoListService, times(1)).getTodoListById(TODO_LIST_ID);
//        verify(todoListService, never()).shareTodoList(SECOND_USER_USERNAME, TODO_LIST_ID, USER_ID);
//        verify(shareService, times(1)).isSharedTodoListToUser(todoList, SECOND_USER_ID);
//        verify(userService, times(1)).getUserByUsername(SECOND_USER_USERNAME);
//
//    }
//
//    @Test
//    public void getTodoListsByName() throws Exception {
//        // arrange
//        List<TodoList> todoLists = createListOfTodoLists();
//
//        when(todoListService.searchTodoListByName(eq(TODO_LIST_NAME + "%"), eq(USER_USERNAME), any(Pageable.class)))
//                .thenReturn(todoLists);
//
//        // act
//        MvcResult result = this.mockMvc.perform(get("/api/todolists/search?name={todoListName}", TODO_LIST_NAME)
//                .with(user(USER_USERNAME))
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        List<TodoList> todoListsFromJsonResponse = jsonParser.getListOfObjectsFromJsonResponse(result.getResponse().getContentAsString());
//
//        // assert
//        verify(todoListService, times(1)).searchTodoListByName(eq(TODO_LIST_NAME + "%"), eq(USER_USERNAME), any(Pageable.class));
//        this.assertListsEqual(todoLists, todoListsFromJsonResponse);
//    }
//
//    private void assertListsEqual(List<TodoList> todoLists1, List<TodoList> todoLists2) {
//        IdComparator idComparator = new IdComparator();
//
//        todoLists1.sort(idComparator);
//        todoLists2.sort(idComparator);
//
//        // assert
//        Assert.assertEquals(todoLists1, todoLists2);
//    }
//}
