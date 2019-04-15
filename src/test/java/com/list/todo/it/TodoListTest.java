package com.list.todo.it;

import com.list.todo.controllers.TodoListController;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.entity.User;
import com.list.todo.init.TodoListInit;
import com.list.todo.payload.TodoListInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import com.list.todo.util.JsonParser;
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
    public void beforeMethod() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(todoListController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
                .build();
    }

    @Test
    public void getTodoLists_getExistentTodoLists_OkStatus() throws Exception {
        List<TodoList> todoLists = TodoListInit.createListOfTodoLists();

        when(todoListService.getTodoListsByUser(any(UserPrincipal.class), any(TodoListStatus.class), any(Pageable.class), anyList()))
                .thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists?status={status}&tagId=", "ACTIVE").with(user("username")))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = JsonParser.getArrayOfTodoListsFromJsonResponse(result.getResponse().getContentAsString(), todoLists.size());

        Assert.assertEquals(todoLists.hashCode(), todoListsFromJsonResponse.hashCode());
    }

    @Test
    public void getMySharedTodoLists_getExistentTodoLists_OkStatus() throws Exception {

        List<TodoList> todoLists = TodoListInit.createListOfTodoLists();

        when(shareService.getSharedTodoListsByUser(anyLong())).thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists/shared"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = JsonParser.getArrayOfTodoListsFromJsonResponse(result.getResponse().getContentAsString(), todoLists.size());

        Assert.assertEquals(todoLists.hashCode(), todoListsFromJsonResponse.hashCode());
    }

    @Test
    public void getTodoList_getExistentTodoList_OkStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));

        MvcResult result = this.mockMvc.perform(get("/api/todolists/{todoListId}", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TodoList todoListFromJsonResponse = JsonParser.getTodoListFromJsonResponse(result.getResponse().getContentAsString());

        Assert.assertEquals(todoList.hashCode(), todoListFromJsonResponse.hashCode());
    }

    @Test
    public void getTodoList_getNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.empty());

        this.mockMvc.perform(get("/api/todolists/{todoListId}", "1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTodoList_getTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setCreatedBy("another username");

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(get("/api/todolists/{todoListId}", "1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void addTodoList_addCorrectTodoList_OkStatus() throws Exception {

        Long userId = 1L;

        TodoList todoList = TodoListInit.createTodoList();
        TodoListInput todoListInput = new TodoListInput(todoList.getTodoListName(), todoList.getComment(), todoList.getTasks());

        when(todoListService.addTodoList(todoListInput, userId)).thenReturn(Optional.of(todoList));

        MvcResult result = this.mockMvc.perform(post("/api/todolists").content("{" +
                "\"todoListName\": \"todoListName\"," +
                "\"tasks\": [ {\"body\": \"task\", \"isComplete\": false, \"priority\":\"NOT_SPECIFIED\"}, {\"body\": \"task\", \"isComplete\": false, \"priority\":\"NOT_SPECIFIED\"} ]" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        TodoList todoListFromJsonResponse = JsonParser.getTodoListFromJsonResponse(result.getResponse().getContentAsString());

        Assert.assertEquals(todoList.hashCode(), todoListFromJsonResponse.hashCode());
    }

    @Test
    public void updateTodoList_updateExistentTodoLists_OkStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(1L);

        TodoList updatedTodoList = TodoListInit.createTodoList();
        updatedTodoList.setId(1L);
        updatedTodoList.setTodoListName("todoListName");
        updatedTodoList.setComment("comment");

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoListService.updateTodoList(anyLong(), any(TodoListInput.class), anyLong())).thenReturn(Optional.of(updatedTodoList));

        this.mockMvc.perform(put("/api/todolists/{todoListId}", "1").content("{\n" +
                "  \"todoListName\":\"todoListName\",\n" +
                "  \"comment\":\"comment\"" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value("todoListName"))
                .andExpect(jsonPath("createdBy").value("username"))
                .andExpect(jsonPath("$.tasks[0].body").value("task"))
                .andExpect(jsonPath("$.tasks[0].isComplete").value("false"))
                .andExpect(jsonPath("$.tasks[0].priority").value("NOT_SPECIFIED"))
                .andExpect(status().isOk());

        verify(todoListService).updateTodoList(anyLong(), any(TodoListInput.class), anyLong());
    }

    @Test
    public void updateTodoList_updateNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/{todoListId}", "1000").content("{\n" +
                "  \"todoListName\":\"mytodolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTodoList_updateTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setCreatedBy("another username");

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(put("/api/todolists/{todoListId}", "6").content("{\n" +
                "  \"todoListName\":\"mytodolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void enableTodoList_enableExistentTodolist_OkStatus() throws Exception {
        Long todoListId = 1L;
        Optional<TodoList> todoList = Optional.of(TodoListInit.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
        Optional<TodoList> enabledTodoList = Optional.of(TodoListInit.createTodoList());
        enabledTodoList.get().setTodoListStatus(TodoListStatus.ACTIVE);

        when(todoListService.getTodoListById(todoListId)).thenReturn(todoList);
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.ACTIVE)).thenReturn(enabledTodoList);

        this.mockMvc.perform(put("/api/todolists/enable/{id}", todoListId))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value("todoListName"))
                .andExpect(jsonPath("createdBy").value("username"))
                .andExpect(status().isOk());
    }

    @Test
    public void enableTodoList_enableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
        Long todoListId = 1L;
        Optional<TodoList> todoList = Optional.of(TodoListInit.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.INACTIVE);
        todoList.get().setCreatedBy("another username");

        when(todoListService.getTodoListById(todoListId)).thenReturn(todoList);

        this.mockMvc.perform(put("/api/todolists/enable/{id}", todoListId))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);

    }

    @Test
    public void enableTodoList_enableNonExistentTodoList_NotFoundStatus() throws Exception {
        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/enable/{id}", todoListId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.ACTIVE);
    }

    @Test
    public void disableTodoList_disableExistentTodolist_OkStatus() throws Exception {
        Long todoListId = 1L;
        Optional<TodoList> todoList = Optional.of(TodoListInit.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
        Optional<TodoList> disabledTodoList = Optional.of(TodoListInit.createTodoList());
        disabledTodoList.get().setTodoListStatus(TodoListStatus.INACTIVE);

        when(todoListService.getTodoListById(todoListId)).thenReturn(todoList);
        when(todoListService.changeTodoListStatus(todoListId, TodoListStatus.INACTIVE)).thenReturn(disabledTodoList);

        this.mockMvc.perform(put("/api/todolists/disable/{id}", todoListId))
                .andDo(print())
                .andExpect(jsonPath("todoListName").value("todoListName"))
                .andExpect(jsonPath("createdBy").value("username"))
                .andExpect(status().isOk());
    }

    @Test
    public void disableTodoList_disableTodoListOfAnotherUser_ForbiddenStatus() throws Exception {
        Long todoListId = 1L;
        Optional<TodoList> todoList = Optional.of(TodoListInit.createTodoList());
        todoList.get().setTodoListStatus(TodoListStatus.ACTIVE);
        todoList.get().setCreatedBy("another username");

        when(todoListService.getTodoListById(todoListId)).thenReturn(todoList);

        this.mockMvc.perform(put("/api/todolists/disable/{id}", todoListId))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);

    }

    @Test
    public void disableTodoList_disableNonExistentTodoList_NotFoundStatus() throws Exception {
        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        this.mockMvc.perform(put("/api/todolists/disable/{id}", todoListId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).changeTodoListStatus(todoListId, TodoListStatus.INACTIVE);
    }

    @Test
    public void deleteTodoList_deleteExistentTodoList_OkStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(1L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(todoListService).deleteTodoList(anyLong(), anyLong());
    }

    @Test
    public void deleteTodoList_deleteNonExistentTodoList_NotFoundStatus() throws Exception {

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "1000"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).deleteTodoList(anyLong(), anyLong());

    }

    @Test
    public void deleteTodoList_deleteTodoListOfAnotherUser_ForbiddenStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setCreatedBy("another username");

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));

        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "6"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).deleteTodoList(anyLong(), anyLong());

    }

    @Test
    public void shareTodoListToUser_shareExistentTodoListToUser_OkStatus() throws Exception {

        User user = new User();
        user.setId(1L);

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(2L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "2", "userName"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(todoListService).shareTodoList(anyString(), anyLong(), anyLong());
    }

    @Test
    public void shareTodoListToUser_shareNonExistentTodoListToUser_NotFoundStatus() throws Exception {

        User user = new User();
        user.setId(1L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.empty());
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "1000", "userName"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).shareTodoList(anyString(), anyLong(), anyLong());

    }

    @Test
    public void shareTodoListToUser_shareTodoListToNonExistentUser_NotFoundStatus() throws Exception {

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(2L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.empty());
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "2", "non existent username"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(todoListService, never()).shareTodoList(anyString(), anyLong(), anyLong());
    }

    @Test
    public void shareTodoListToUser_shareMyTodoListToMyself_ForbiddenStatus() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(2L);
        todoList.setCreatedBy("username");

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "2", "username"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).shareTodoList(anyString(), anyLong(), anyLong());
    }

    @Test
    public void shareTodoListToUser_shareTodoListToMyself_ForbiddenStatus() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(2L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(false);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "2", "username"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(todoListService, never()).shareTodoList(anyString(), anyLong(), anyLong());
    }

    @Test
    public void shareTodoListToUser_shareTodoListToAnotherUserMoreThanOneTime_ConflictStatus() throws Exception {
        User user = new User();
        user.setId(1L);

        TodoList todoList = TodoListInit.createTodoList();
        todoList.setId(2L);

        when(todoListService.getTodoListById(anyLong())).thenReturn(Optional.of(todoList));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(shareService.isSharedTodoListToUser(any(TodoList.class), anyLong())).thenReturn(true);

        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "2", "userName"))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(todoListService, never()).shareTodoList(anyString(), anyLong(), anyLong());
    }

    @Test
    public void getTodoListsByName() throws Exception {
        List<TodoList> todoLists = TodoListInit.createListOfTodoLists();

        when(todoListService.searchTodoListByName(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(todoLists);

        MvcResult result = this.mockMvc.perform(get("/api/todolists/search?name={todoListName}", "todoList").with(user("username")))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<TodoList> todoListsFromJsonResponse = JsonParser.getArrayOfTodoListsFromJsonResponse(result.getResponse().getContentAsString(), todoLists.size());

        Assert.assertEquals(todoLists.hashCode(), todoListsFromJsonResponse.hashCode());
    }
}
