package com.list.todo.configurations;

import com.list.todo.graphql.*;
import com.list.todo.repositories.*;
import com.list.todo.security.JwtTokenProvider;
import com.list.todo.services.FollowerService;
import com.list.todo.services.ShareService;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.GraphQLErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class GraphQLConfig {

    @Bean
    public TaskResolver taskResolver(TodoListRepository todoListRepository) {
        return new TaskResolver(todoListRepository);
    }

    @Bean
    public TodoListResolver todoListResolver(TaskRepository taskRepository) {
        return new TodoListResolver(taskRepository);
    }

    @Bean
    public UserQuery userQuery(UserRepository userRepository, FollowerRepository followerRepository, TodoListRepository todoListRepository,
                           ShareRepository shareRepository) {
        return new UserQuery(userRepository, followerRepository, todoListRepository, shareRepository);
    }

    @Bean
    public TodoListQuery todoListQuery(TodoListRepository todoListRepository, ShareRepository shareRepository) {
        return new TodoListQuery(todoListRepository, shareRepository);
    }

    @Bean
    public TaskQuery taskQuery(TaskRepository taskRepository, TodoListRepository todoListRepository) {
        return new TaskQuery(taskRepository, todoListRepository);
    }

    @Bean
    public UserMutation userMutation(UserRepository userRepository, FollowerRepository followerRepository,
                                     AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
                                     JwtTokenProvider tokenProvider) {
        return new UserMutation(authenticationManager, passwordEncoder, tokenProvider, userRepository, followerRepository);
    }

    @Bean
    public TodoListMutation todoListMutation(UserRepository userRepository, TodoListRepository todoListRepository,
                                             ShareService shareService, FollowerService followerService) {
        return new TodoListMutation(userRepository, todoListRepository, shareService, followerService);
    }

    @Bean
    public TaskMutation taskMutation(TodoListRepository todoListRepository, TaskRepository taskRepository) {
        return new TaskMutation(todoListRepository, taskRepository);
    }

    @Bean
    public GraphQLErrorHandler errorHandler() {
        return new GraphQLErrorHandler() {
            @Override
            public List<GraphQLError> processErrors(List<GraphQLError> errors) {
                List<GraphQLError> clientErrors = errors.stream()
                        .filter(this::isClientError)
                        .collect(Collectors.toList());

                List<GraphQLError> serverErrors = errors.stream()
                        .filter(e -> !isClientError(e))
                        .map(GraphQLErrorAdapter::new)
                        .collect(Collectors.toList());

                List<GraphQLError> e = new ArrayList<>();
                e.addAll(clientErrors);
                e.addAll(serverErrors);
                return e;
            }

            private boolean isClientError(GraphQLError error) {
                return !(error instanceof ExceptionWhileDataFetching || error instanceof Throwable);
            }
        };
    }
}
