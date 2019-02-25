/*
package com.list.todo.it;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yml")
@Sql(value = {"/create-user-before.sql", "/create-todolists-before.sql", "/create-task-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-todolists-after.sql", "/create-user-after.sql", "/create-task-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails(value = "stepanich")
public class TaskTest {

	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void getAllTasksOnTodoList() throws Exception {
		this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", "4"))
				.andDo(print())
				.andExpect(authenticated())
				*/
/*.andExpect(content().string(containsString("ggggg")))
				.andExpect(content().string(containsString("zzzzz")))*//*


				// TODO: достать через мап массив и сравнить через Assert
				*/
/*.andExpect(jsonPath("$[0].body").value("ggggg"))
				.andExpect(jsonPath("$[0].isComplete").value("false"))
				.andExpect(jsonPath("$[1].body").value("zzzzz"))
				.andExpect(jsonPath("$[1].isComplete").value("false"))*//*

				.andExpect(status().isOk());
	}

	@Test
	public void getTasksOnNonExistentTodoList() throws Exception {
		this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", "1000"))
				.andDo(print())
				.andExpect(authenticated())
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void getTasksOnTodoListOfAnotherUser() throws Exception {
		this.mockMvc.perform(get("/api/tasks?todoListId={todoListId}", "6"))
				.andDo(print())
				.andExpect(authenticated())
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void addTask() throws Exception {
		this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", "4").content("{\n" + 
				"  \"body\":\"task\",\n" + 
				"  \"isComplete\":\"false\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(jsonPath("body").value("task"))
				.andExpect(jsonPath("isComplete").value("false"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void addTaskOnNonExistentTodoList() throws Exception {
		this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", "1000").content("{\n" + 
				"  \"body\":\"task\",\n" + 
				"  \"isComplete\":\"false\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void addTaskOnTodoListOfAnotherUser() throws Exception {
		this.mockMvc.perform(post("/api/tasks?todoListId={todoListId}", "6").content("{\n" + 
				"  \"body\":\"task\",\n" + 
				"  \"isComplete\":\"false\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void updateTask() throws Exception {
		this.mockMvc.perform(put("/api/tasks/{taskId}", "8").content("{\n" + 
				"  \"body\":\"task\",\n" + 
				"  \"isComplete\":\"true\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(jsonPath("id").value("8"))
				.andExpect(jsonPath("body").value("task"))
				.andExpect(jsonPath("isComplete").value("true"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void updateNonExistentTask() throws Exception {
		this.mockMvc.perform(put("/api/tasks/{taskId}", "1000").content("{\n" + 
				"  \"body\":\"task\",\n" + 
				"  \"isComplete\":\"true\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateTaskOnTodoListOfAnotherUser() throws Exception {
		this.mockMvc.perform(put("/api/tasks/{taskId}", "10").content("{\n" + 
				"  \"body\":\"todolist\",\n" + 
				"  \"isComplete\":\"true\"" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andDo(print())
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteTask() throws Exception {
		this.mockMvc.perform(delete("/api/tasks/{taskId}", "8"))
				.andDo(print())
				.andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteTaskNonExistentTask() throws Exception {
		this.mockMvc.perform(delete("/api/tasks/{taskId}", "1000"))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteTaskOnTodoListOfAnotherUser() throws Exception {
		this.mockMvc.perform(delete("/api/tasks/{taskId}", "10"))
				.andDo(print())
				.andExpect(status().isForbidden());
	}
	
}
*/
