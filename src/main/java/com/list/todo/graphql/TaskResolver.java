package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TodoListRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskResolver implements GraphQLResolver<Task> {
	
	private TodoListRepository todoListRepository;
	
	public TodoList getTodoList(Task task) {
		return todoListRepository.findById(task.getTodoList().getId()).orElse(null);
	}
	
}
