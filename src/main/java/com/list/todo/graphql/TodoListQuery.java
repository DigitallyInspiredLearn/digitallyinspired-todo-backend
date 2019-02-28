package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.ShareService;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TodoListQuery implements GraphQLQueryResolver {

	private TodoListService todoListService;
	private UserService userService;
	private ShareService shareService;

	public Iterable<TodoList> getMyTodoLists() {
		UserPrincipal currentUser = userService.getCurrentUser();
		return todoListService.getTodoListsByUser(currentUser.getId(), Pageable.unpaged());
	}

    public Iterable<TodoList> getMySharedTodoLists() {
        UserPrincipal currentUser = userService.getCurrentUser();
		return shareService.getSharedTodoListsByUser(currentUser.getId());
	}

    public TodoList getTodoList(Long todoListId) {
        return todoListService.getTodoListById(todoListId).orElse(null);
    }

}
