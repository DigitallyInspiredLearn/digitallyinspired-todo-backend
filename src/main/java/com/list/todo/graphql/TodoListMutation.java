package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TodoListService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TodoListMutation implements GraphQLMutationResolver {

    private TodoListService todoListService;
    private UserService userService;

    public boolean addTodoList(TodoListInput todoListInput) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return todoListService.addTodoList(todoListInput, currentUser.getId()).isPresent();
    }

    public boolean updateTodoList(Long todoListId, TodoListInput todoListInput) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return todoListService.updateTodoList(todoListId, todoListInput, currentUser.getId()).isPresent();
    }

    public void deleteTodoList(Long todoListId) {
        UserPrincipal currentUser = userService.getCurrentUser();
        todoListService.deleteTodoList(todoListId, currentUser.getId());
    }

    public ApiResponse shareTodoListToUser(String sharedUsername, Long sharedTodoListId) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return todoListService.shareTodoList(sharedUsername, sharedTodoListId, currentUser.getId());
    }

}
