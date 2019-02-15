package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TodoListRepository;

import com.list.todo.services.TodoListService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskResolver implements GraphQLResolver<Task> {

	private TodoListService todoListService;
	
	public TodoList getTodoList(Task task) {
		return todoListService.getTodoListById(task.getTodoList().getId()).orElse(null);
	}
	
}
