package com.list.todo.controllers;

import com.list.todo.services.TodoListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
public class TodoListControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TodoListService todoListService;

    @MockBean
    private Pageable pageable;


    @Test
    public void getMyTodoLists() throws Exception {
//        String username = "string";
//        TodoList todoList1 = new TodoList();
//        todoList1.setTodoListName("name1");
//        TodoList todoList2 = new TodoList();
//        todoList2.setTodoListName("name2");
//        List<TodoList> todoLists = new ArrayList<>();
//        todoLists.add(todoList1);
//        todoLists.add(todoList2);
//        Page<TodoList> todoListPage = new PageImpl<>(todoLists, pageable, todoLists.size());
//
//        given(this.todoListService.getTodoListsByUser(username, pageable)).willReturn(todoListPage);
//
//        this.mvc.perform(get("/api/todolists/my"))
//                .andExpect(status().isOk())
//                .andExpect(content().json(String.valueOf(todoListPage)));
    }

    @Test
    public void getMySharedTodoLists() {
    }

    @Test
    public void getTodoList() {
    }

    @Test
    public void addTodoList() {
    }

    @Test
    public void updateTodoList() {
    }

    @Test
    public void deleteTodoList() {
    }

    @Test
    public void shareTodoListToUser() {
    }
}