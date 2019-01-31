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
@Sql(value = "/create-user-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/create-user-after.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails(value = "stepanich")
public class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	public void userInfo() throws Exception {
		this.mockMvc.perform(get("/api/users/me"))
			.andDo(print())
			.andExpect(authenticated())
			.andExpect(jsonPath("name").value("Stepan Matveev"))
//			.andExpect(content().)
			.andExpect(status().isOk());
		
	}

	@Test
	public void serchUsersByUsername() throws Exception {
		this.mockMvc.perform(get("/api/users/search?username={username}", "ann"))
			.andDo(print())
			.andExpect(authenticated())
//			.andExpect(jsonPath("$", is).value("anna"))
			.andExpect(jsonPath(".username").value("anna"))
			.andExpect(status().isOk());

	}
}
