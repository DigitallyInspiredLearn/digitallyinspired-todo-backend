package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TaskRepository;

import com.list.todo.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TodoListResolver implements GraphQLResolver<TodoList> {

	private TaskService taskService;
	
	public Iterable<Task> getTasks(TodoList todoList) {
		return taskService.getAllTasksOnTodoList(todoList.getId());
	}
	
}
