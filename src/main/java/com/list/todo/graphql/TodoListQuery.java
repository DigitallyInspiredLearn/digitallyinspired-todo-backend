package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Component
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TodoListQuery implements GraphQLQueryResolver {

	private TodoListRepository todoListRepository;
	private ShareRepository shareRepository;

	public Iterable<TodoList> getMyTodoLists() {

		UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return todoListRepository.findTodoListsByUserOwnerId(user.getId());
	}

    public Iterable<TodoList> getMySharedTodoLists() {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return shareRepository.findBySharedUserId(user.getId()).stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
	}

    public TodoList getTodoList(Long todoListId) {
        return todoListRepository.findById(todoListId).orElse(null);
    }

}
