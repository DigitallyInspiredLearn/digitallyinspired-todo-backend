package com.list.todo.it;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.TodoListApplication;
import com.list.todo.configurations.H2TestProfileJPAConfig;
import com.list.todo.controllers.TasksController;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
import com.list.todo.services.TodoListService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TodoListApplication.class, H2TestProfileJPAConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final String CURRENT_USER_USERNAME = "username";
	private static final String ANOTHER_USER_USERNAME = "another_username";
    private static final Long TODO_LIST_ID = 3L;
    private static final Long NOT_EXISTENT_TODO_LIST_ID = 3L;
	private static final Long TASK_ID = 11L;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private TodoListService todoListService;

    @InjectMocks
    private TasksController tasksController;

    private ObjectMapper objectMapper = new ObjectMapper();


    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(CURRENT_USER_ID);
            userPrincipal.setUsername(CURRENT_USER_USERNAME);
            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("password"));

            return userPrincipal;
        }
    };

    private HandlerMethodArgumentResolver putPageable = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(
                MethodParameter parameter) {
            return parameter.getParameterType().equals(
                    Pageable.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory) {

            return PageRequest.of(0, 50);
        }
    };

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tasksController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putPageable)
                .build();
    }

	@Test
	public void getAllTasksOnTodoList_OnExistentTodoList_ReturnsAListOfTasks() throws Exception {
        //arrange
        TodoList todoList = createTodoList();
        todoList.setCreatedBy(CURRENT_USER_USERNAME);
        todoList.setId(TODO_LIST_ID);
        List<Task> tasks = createListOfTasks();

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(taskService.getAllTasksOnTodoList(TODO_LIST_ID)).thenReturn(tasks);

        //act
        MvcResult mvcResult = this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", TODO_LIST_ID))
				.andDo(print())
				.andExpect(status().isOk())
                .andReturn();
        List<Task> returnedTasks = getListOfTasksFromJsonResponse(mvcResult.getResponse().getContentAsString());

        //assert
        Assert.assertEquals(tasks, returnedTasks);
        verify(todoListService).getTodoListById(TODO_LIST_ID);
        verify(taskService).getAllTasksOnTodoList(TODO_LIST_ID);
	}

	@Test
	public void getAllTasksOnTodoList_OnNonExistentTodoList_ReturnsStatusNotFound() throws Exception {
        //arrange
        when(todoListService.getTodoListById(NOT_EXISTENT_TODO_LIST_ID)).thenReturn(Optional.empty());

        //act, assert
		this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", NOT_EXISTENT_TODO_LIST_ID))
				.andDo(print())
				.andExpect(status().isNotFound());
		verify(todoListService).getTodoListById(NOT_EXISTENT_TODO_LIST_ID);
		verify(taskService, times(0)).getAllTasksOnTodoList(NOT_EXISTENT_TODO_LIST_ID);
	}

	@Test
	public void getAllTasksOnTodoList_OnTodoListCreatedByAnotherUser_ReturnsStatusForbidden() throws Exception {
		//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(ANOTHER_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);

		when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

		//act, assert
		this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", TODO_LIST_ID))
				.andDo(print())
				.andExpect(status().isForbidden());
		verify(todoListService).getTodoListById(TODO_LIST_ID);
		verify(taskService, times(0)).getAllTasksOnTodoList(TODO_LIST_ID);
	}

	@Test
	public void addTask_OnExistentTodoList_ReturnsAddedTask() throws Exception {
    	//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(CURRENT_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		Task task = createTask();
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
		when(taskService.addTask(taskInput)).thenReturn(Optional.of(task));

		//act
		MvcResult result = this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", TODO_LIST_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
		Task returnedTask = objectMapper.readValue(result.getResponse().getContentAsString(), Task.class);

		//assert
		Assert.assertEquals(task, returnedTask);
		verify(todoListService).getTodoListById(TODO_LIST_ID);
		verify(taskService).addTask(taskInput);
	}

	@Test
	public void addTask_OnNonExistentTodoList_ReturnsStatusNotFound() throws Exception {
		//arrange
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

		//act
		this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", TODO_LIST_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isNotFound());

		//assert
		verify(todoListService).getTodoListById(TODO_LIST_ID);
		verify(taskService, times(0)).addTask(taskInput);
	}

	@Test
	public void addTask_OnTodoListCreatedByAnotherUser_ReturnsStatusForbidden() throws Exception {
		//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(ANOTHER_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));

		//act
		this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", TODO_LIST_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isForbidden());

		//assert
		verify(todoListService).getTodoListById(TODO_LIST_ID);
		verify(taskService, times(0)).addTask(taskInput);
	}

	@Test
	public void updateTask_OnExistentTask_ReturnsUpdatedTask() throws Exception {
		//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(CURRENT_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		Task task = createTask();
		task.setId(TASK_ID);
		task.setTodoList(todoList);
		Task updatedTask = createTask();
		updatedTask.setBody("updated_body");
		updatedTask.setId(TASK_ID);
		updatedTask.setTodoList(todoList);
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));
		when(taskService.updateTask(TASK_ID, taskInput)).thenReturn(Optional.of(updatedTask));

		//act
		MvcResult result = this.mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
		Task returnedTask = objectMapper.readValue(result.getResponse().getContentAsString(), Task.class);

		//assert
		Assert.assertEquals(updatedTask, returnedTask);
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService).updateTask(TASK_ID, taskInput);
	}

	@Test
	public void updateTask_OnNonExistentTask_ReturnsStatusNotFound() throws Exception {
		//arrange
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.empty());

		//act
		this.mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andReturn();

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).updateTask(TASK_ID, taskInput);
	}

	@Test
	public void updateTask_OnNonExistentTodoList_ReturnsStatusNotFound() throws Exception {
		//arrange
		Task task = createTask();
		task.setId(TASK_ID);
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

		//act
		this.mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andReturn();

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).updateTask(TASK_ID, taskInput);
	}

	@Test
	public void updateTask_OnTodoListCreatedByAnotherUser_ReturnsStatusForbidden() throws Exception {
		//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(ANOTHER_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		Task task = createTask();
		task.setId(TASK_ID);
		task.setTodoList(todoList);
		TaskInput taskInput = createTaskInput(TODO_LIST_ID);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

		//act
		this.mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
				.content(objectMapper.writeValueAsString(taskInput))
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andReturn();

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).updateTask(TASK_ID, taskInput);
	}

	@Test
	public void deleteTask_OnExistentTask_SuccessfulDelete() throws Exception {
    	//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(CURRENT_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		Task task = createTask();
		task.setId(TASK_ID);
		task.setTodoList(todoList);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

		//act
		this.mockMvc.perform(delete("/api/tasks/{taskId}", TASK_ID))
				.andDo(print())
				.andExpect(status().isNoContent());

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService).deleteTask(TASK_ID);
	}

	@Test
	public void deleteTask_OnNonExistentTask_ReturnsStatusNotFound() throws Exception {
		//arrange
		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.empty());

		//act
		this.mockMvc.perform(delete("/api/tasks/{taskId}", TASK_ID))
				.andDo(print())
				.andExpect(status().isNotFound());

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).deleteTask(TASK_ID);
	}

	@Test
	public void deleteTask_OnNonExistentTodoList_ReturnsStatusNotFound() throws Exception {
		//arrange
		Task task = createTask();
		task.setId(TASK_ID);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

		//act
		this.mockMvc.perform(delete("/api/tasks/{taskId}", TASK_ID))
				.andDo(print())
				.andExpect(status().isNotFound());

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).deleteTask(TASK_ID);
	}

	@Test
	public void deleteTask_OnTodoListCreatedByAnotherUser_ReturnsStatusForbidden() throws Exception {
		//arrange
		TodoList todoList = createTodoList();
		todoList.setCreatedBy(ANOTHER_USER_USERNAME);
		todoList.setId(TODO_LIST_ID);
		Task task = createTask();
		task.setId(TASK_ID);
		task.setTodoList(todoList);

		when(taskService.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

		//act
		this.mockMvc.perform(delete("/api/tasks/{taskId}", TASK_ID))
				.andDo(print())
				.andExpect(status().isForbidden());

		//assert
		verify(taskService).getTaskById(TASK_ID);
		verify(taskService, times(0)).deleteTask(TASK_ID);
	}

    private List<Task> getListOfTasksFromJsonResponse(String response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class);

        return objectMapper.readValue(response, type);
    }

}
