package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.Task;
import com.list.todo.payload.InputTask;
import com.list.todo.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
@Component
public class TaskMutation implements GraphQLMutationResolver {

    private TaskService taskService;

    public Task addTask(InputTask inputTask) {
        return taskService.addTask(inputTask);
    }

    public Task updateTask(Long currentTaskId, InputTask inputTask) {
        return taskService.updateTask(currentTaskId, inputTask);
    }

    public boolean deleteTask(Long taskId) {
        return taskService.deleteTask(taskId);
    }
}
