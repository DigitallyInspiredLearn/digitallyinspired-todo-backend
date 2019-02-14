package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.ApiResponse;
<<<<<<< HEAD
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.ShareService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
=======
import com.list.todo.payload.InputTodoList;
import com.list.todo.services.TodoListService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
>>>>>>> refactored
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Component
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TodoListMutation implements GraphQLMutationResolver {

    private TodoListService todoListService;

    public TodoList addTodoList(InputTodoList inputTodoList) {
        return todoListService.addTodoList(inputTodoList);
    }

    public TodoList updateTodoList(Long todoListId, InputTodoList inputTodoList) {
        return todoListService.updateTodoList(todoListId, inputTodoList);
    }

    public boolean deleteTodoList(Long todoListId) {
        return todoListService.deleteTodoList(todoListId);
    }

    public ApiResponse shareTodoListToUser(String sharedUsername, Long sharedTodoListId) {
        return todoListService.shareTodoList(sharedUsername, sharedTodoListId);
    }

}
