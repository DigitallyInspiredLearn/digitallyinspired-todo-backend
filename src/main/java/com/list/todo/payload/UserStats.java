package com.list.todo.payload;

import java.util.List;

import com.list.todo.entity.TodoList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStats {
	private List<TodoList> myTodoLists;
	private List<TodoList> sharedTodoLists;

}
