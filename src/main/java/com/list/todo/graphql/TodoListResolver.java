package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TaskRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class TodoListResolver implements GraphQLResolver<TodoList> {
	
	private TaskRepository taskRepository;
	
	public Iterable<Task> getTasks(TodoList todoList) {
		return taskRepository.findTasksByTodoListId(todoList.getId());
	}
	
}
