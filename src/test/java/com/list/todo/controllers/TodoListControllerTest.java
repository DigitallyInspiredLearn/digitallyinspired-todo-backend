package com.list.todo.controllers;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-todolists-before.sql", "/create-task-before.sql", "/create-shares-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-todolists-after.sql", "/create-user-after.sql", "/create-task-after.sql", "/create-shares-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails(value = "stepanich")
public class TodoListControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void getTodoListsByUser() throws Exception {
        this.mockMvc.perform(get("/api/todolists/my"))
                .andDo(print())
                .andExpect(authenticated())
//                .andExpect(jsonPath("$[0].id").value("4"))
//                .andExpect(jsonPath("$[0].todoListName").value("tl1"))
//                .andExpect(jsonPath("$[0].userOwnerId").value("1"))
//                .andExpect(jsonPath("$[0].tasks[0].id").value("8"))
//                .andExpect(jsonPath("$[0].tasks[1].id").value("9"))
//                .andExpect(jsonPath("$[1].id").value("5"))
//                .andExpect(jsonPath("$[1].todoListName").value("tl2"))
//                .andExpect(jsonPath("$[1].userOwnerId").value("1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getSharedTodolists() throws Exception {
        this.mockMvc.perform(get("/api/todolists/shared"))
                .andDo(print())
                .andExpect(authenticated())
//                .andExpect(jsonPath("$[0].id").value("6"))
//                .andExpect(jsonPath("$[0].todoListName").value("tl3"))
//                .andExpect(jsonPath("$[0].userOwnerId").value("2"))
//                .andExpect(jsonPath("$[0].tasks[0].id").value("10"))
//                .andExpect(jsonPath("$[1].id").value("7"))
//                .andExpect(jsonPath("$[1].todoListName").value("tl4"))
//                .andExpect(jsonPath("$[1].userOwnerId").value("2"))
                .andExpect(status().isOk());
    }

    @Test
    public void getTodoListById() throws Exception {
        this.mockMvc.perform(get("/api/todolists/{todoListId}", "4"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(jsonPath("id").value("4"))
                .andExpect(jsonPath("todoListName").value("tl1"))
                .andExpect(jsonPath("userOwnerId").value("1"))
                .andExpect(jsonPath("tasks[0].id").value("8"))
                .andExpect(jsonPath("tasks[1].id").value("9"))
                .andExpect(status().isOk());
    }

    @Test
    public void getNonExistentTodoListById() throws Exception {
        this.mockMvc.perform(get("/api/todolists/{todoListId}", "1000"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTodoListOfAnotherUserById() throws Exception {
        this.mockMvc.perform(get("/api/todolists/{todoListId}", "6"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isForbidden());
    }

    @Test
    public void addTodoList() throws Exception {
        this.mockMvc.perform(post("/api/todolists").content("{\n" +
                "  \"todoListName\":\"todolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(jsonPath("id").value("100"))
                .andExpect(jsonPath("todoListName").value("todolist"))
                .andExpect(jsonPath("userOwnerId").value("1"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateTodoList() throws Exception {
        this.mockMvc.perform(put("/api/todolists/{todoListId}", "4").content("{\n" +
                "  \"todoListName\":\"mytodolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(jsonPath("id").value("4"))
                .andExpect(jsonPath("todoListName").value("mytodolist"))
                .andExpect(jsonPath("userOwnerId").value("1"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateNonExistentTodoList() throws Exception {
        this.mockMvc.perform(put("/api/todolists/{todoListId}", "1000").content("{\n" +
                "  \"todoListName\":\"mytodolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTodoListOfAnotherUser() throws Exception {
        this.mockMvc.perform(put("/api/todolists/{todoListId}", "6").content("{\n" +
                "  \"todoListName\":\"mytodolist\"\n" +
                "}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteTodoList() throws Exception {
        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "4"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteNonExistentTodoList() throws Exception {
        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "1000"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTodoListOfAnotherUser() throws Exception {
        this.mockMvc.perform(delete("/api/todolists/{todoListId}", "6"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shareTodoListToUser() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "4", "anna"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shareNonExistentTodoListToUser() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "1000", "anna"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shareTodoListToNonExistentUser() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "4", "annaanna"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shareTodoListOfAnotherUser() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "6", "anna"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shareTodoListToMyself() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "4", "stepanich"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shareTodoListToUser_shareTodoListToAnotherUserMoreThanOneTime() throws Exception {
        this.mockMvc.perform(post("/api/todolists/{todoListId}/share?username={username}", "4", "vitaliy"))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}
