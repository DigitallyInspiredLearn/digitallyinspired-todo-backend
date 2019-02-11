package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.payload.LoginRequest;
import com.list.todo.payload.RegisterRequest;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.JwtTokenProvider;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@AllArgsConstructor
public class TaskMutation implements GraphQLMutationResolver {

    private TodoListRepository todoListRepository;
    private TaskRepository taskRepository;

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public Task addTask(Long todoListId, String body, Boolean isComplete) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TodoList todoList = todoListRepository.findById(todoListId).orElse(null);
        Task newTask = new Task();

        if (todoList != null && todoList.getUserOwnerId().equals(user.getId())) {
            newTask.setBody(body);
            newTask.setIsComplete(isComplete);
            newTask.setTodoList(todoListRepository.findById(todoListId).orElse(null));
            newTask = taskRepository.save(newTask);

        }
        return newTask;
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public Task updateTask(Long taskId, String body, Boolean isComplete) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId).orElse(null);
        TodoList todoList;

        if (task != null) {

            todoList = task.getTodoList();

            if (todoList != null && todoList.getUserOwnerId().equals(user.getId())) {
                task.setBody(body);
                task.setIsComplete(isComplete);
                task = taskRepository.save(task);
            }
        }
        return task;
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public boolean deleteTask(Long taskId) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId).orElse(null);
        TodoList todoList;
        boolean isSuccess = false;

        if (task != null) {

            todoList = todoListRepository.findById(task.getTodoList().getId()).orElse(null);

            if (todoList != null && todoList.getUserOwnerId().equals(user.getId())) {
                taskRepository.deleteById(taskId);
                isSuccess = true;
            }
        }
        return isSuccess;
    }
}
