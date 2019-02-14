package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.Share;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.*;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
@Component
public class TaskQuery implements GraphQLQueryResolver {

	private TaskRepository taskRepository;
    private TodoListRepository todoListRepository;

    public Iterable<Task> getAllTasksOnTodoList(Long todoListId) {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TodoList todoList = todoListRepository.findById(todoListId).orElse(null);
        Iterable<Task> tasks = null;

        if (todoList != null && todoList.getUserOwnerId().equals(user.getId())) {
            tasks = taskRepository.findTasksByTodoListId(todoListId);
        }

        return tasks;
	}
}
