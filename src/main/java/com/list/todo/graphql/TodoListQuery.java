package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.UserStats;
import com.list.todo.payload.UserSummary;
import com.list.todo.repositories.FollowerRepository;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TodoListQuery implements GraphQLQueryResolver {

	private TodoListRepository todoListRepository;
	private ShareRepository shareRepository;

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	public Iterable<TodoList> getMyTodoLists() {

		UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return todoListRepository.findTodoListsByUserOwnerId(user.getId());
	}

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public Iterable<TodoList> getMySharedTodoLists() {

        UserPrincipal user = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return shareRepository.findBySharedUserId(user.getId()).stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
	}

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public TodoList getTodoList(Long todoListId) {
        return todoListRepository.findById(todoListId).orElse(null);
    }

}
