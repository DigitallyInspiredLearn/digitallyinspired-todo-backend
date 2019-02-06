package com.list.todo.payload;

import java.util.List;

import com.list.todo.entity.TodoList;

import lombok.Data;

@Data
public class UserStats {
	private List<TodoList> myTodoLists;
	private List<TodoList> sharedTodoLists;

}
