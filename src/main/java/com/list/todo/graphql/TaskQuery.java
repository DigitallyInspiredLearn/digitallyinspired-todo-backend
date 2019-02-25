package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.Task;
import com.list.todo.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TaskQuery implements GraphQLQueryResolver {

    private TaskService taskService;

    public Iterable<Task> getAllTasksOnTodoList(Long todoListId) {
        return taskService.getAllTasksOnTodoList(todoListId);
	}
}
