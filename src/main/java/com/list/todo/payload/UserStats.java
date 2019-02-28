package com.list.todo.payload;

import com.list.todo.entity.TodoList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStats {
	private Page<TodoList> myTodoLists;
	private Page<TodoList> sharedTodoLists;
}
