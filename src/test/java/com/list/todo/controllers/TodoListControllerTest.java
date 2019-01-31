package com.list.todo.controllers;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-todolists-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-todolists-after.sql", "/create-user-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails(value = "stepanich")
public class TodoListControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void getTodoListsByUser() throws Exception {
		this.mockMvc.perform(get("/api/todolists/my"))
				.andDo(print())
				.andExpect(authenticated())
				.andExpect(jsonPath("$[0].todoListName").value("tl1"))
				.andExpect(jsonPath("$[0].userOwnerId").value("1"))
				.andExpect(jsonPath("$[1].todoListName").value("tl2"))
				.andExpect(jsonPath("$[1].userOwnerId").value("1"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void getSharedTodolists() throws Exception {
		
	}
	
	@Test
	public void getTodoListById() throws Exception {
		
	}
	
	@Test
	public void addTodoList() throws Exception {
		
	}
	
	@Test
	public void shareTodoListToUser() throws Exception {
		
	}
	
	@Test
	public void updateTodoList() throws Exception {
		
	}
	
	@Test
	public void deleteTodoList() throws Exception {
		
	}
}
