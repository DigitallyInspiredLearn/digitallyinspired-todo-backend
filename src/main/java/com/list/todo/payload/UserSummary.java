package com.list.todo.payload;

import java.util.Set;

import com.list.todo.entity.TodoList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserSummary {
	
    private Long id;
    private String username;
    private String name;
    private Set<TodoList> todoLists;
}
