package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.Task;
import com.list.todo.payload.TaskInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TaskService;
import com.list.todo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
@Component
public class TaskMutation implements GraphQLMutationResolver {

    private TaskService taskService;
    private UserService userService;

    public Task addTask(TaskInput taskInput) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return taskService.addTask(taskInput, currentUser);
    }

    public Task updateTask(Long currentTaskId, TaskInput taskInput) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return taskService.updateTask(currentTaskId, taskInput, currentUser);
    }

    public boolean deleteTask(Long taskId) {
        UserPrincipal currentUser = userService.getCurrentUser();
        return taskService.deleteTask(taskId, currentUser);
    }
}
