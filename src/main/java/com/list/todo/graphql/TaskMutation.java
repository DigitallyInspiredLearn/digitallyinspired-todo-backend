package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.*;
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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TaskMutation implements GraphQLMutationResolver {

    private TodoListRepository todoListRepository;
    private TaskRepository taskRepository;

    public Task addTask(TaskInput taskInput) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TodoList todoList = todoListRepository.findById(taskInput.getTodoListId()).orElse(null);
        Task newTask = new Task();

        if (todoList != null && todoList.getUserOwnerId().equals(user.getId())) {
            newTask.setBody(taskInput.getBody());
            newTask.setIsComplete(false);
            newTask.setTodoList(todoListRepository.findById(taskInput.getTodoListId()).orElse(null));
            newTask = taskRepository.save(newTask);

        }
        return newTask;
    }

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
