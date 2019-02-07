package com.list.todo.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void registerUser() throws Exception {
		this.mockMvc.perform(post("/api/auth/register").content("{\n" + 
				"	\"name\": \"Vitaliy\",\n" + 
				"	\"username\": \"vital\",\n" + 
				"	\"email\": \"cheremushev@yandex.com\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(jsonPath("success").value("true"))	
			.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void registerUserWithExistUsername() throws Exception {
		this.mockMvc.perform(post("/api/auth/register").content("{\n" + 
				"	\"name\": \"Vitaliy\",\n" + 
				"	\"username\": \"vitaliy\",\n" + 
				"	\"email\": \"cheremushev@yandex.com\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(jsonPath("message").value("Username is already taken!"))	
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void registerUserWithExistEmail() throws Exception {
		this.mockMvc.perform(post("/api/auth/register").content("{\n" + 
				"	\"name\": \"Stepan Matveev\",\n" + 
				"	\"username\": \"stepan\",\n" + 
				"	\"email\": \"stepa.matv72@gmail.com\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}")
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(jsonPath("message").value("Email Address already in use!"))	
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void loginUserByUsername() throws Exception {
		this.mockMvc.perform(post("/api/auth/login").content("{\n" + 
				"	\"usernameOrEmail\": \"anna\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(jsonPath("tokenType").value("Bearer"))	
			.andExpect(status().isOk());
	}
	

	@Test
	public void loginUserByEmail() throws Exception {
		this.mockMvc.perform(post("/api/auth/login").content("{\n" + 
				"	\"usernameOrEmail\": \"anna.bogdanova@mail.ru\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(jsonPath("tokenType").value("Bearer"))	
			.andExpect(status().isOk());
	}
	
	@Test
	public void loginUserWithWrongUsername() throws Exception {
		this.mockMvc.perform(post("/api/auth/login").content("{\n" + 
				"	\"usernameOrEmail\": \"anastasiya\",\n" + 
				"	\"password\": \"Password\"\n" + 
				"}").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void loginUserWithWrongPassword() throws Exception {
		this.mockMvc.perform(post("/api/auth/login").content("{\n" + 
				"	\"usernameOrEmail\": \"anna\",\n" + 
				"	\"password\": \"Pass\"\n" + 
				"}").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
			.andDo(print())
			.andExpect(status().is4xxClientError());
	}

}

