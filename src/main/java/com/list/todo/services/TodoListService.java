package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;

    private FollowerService followerService;
    private UserService userService;
    private ShareService shareService;
    private final NotificationService notificationService;

    public Optional<TodoList> getTodoListById(Long todoListId) {
        return todoListRepository.findById(todoListId);
    }

    public Iterable<TodoList> getTodoListsByUser(Long userId) {
        return todoListRepository.findTodoListsByUserOwnerId(userId);
    }

    public Optional<TodoList> addTodoList(TodoListInput todoListInput, Long userId) {

        TodoList todoList = new TodoList();
        todoList.setUserOwnerId(userId);
        todoList.setTodoListName(todoListInput.getTodoListName());

        userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyFollowersAboutAddingTodolist(user, todoList));

        return Optional.of(todoListRepository.save(todoList));
    }

    public Optional<TodoList> updateTodoList(Long todoListId, TodoListInput todoListInput, Long userId) {

        Optional<TodoList> todoList = todoListRepository.findById(todoListId)
                .map(tl -> {
                    tl.setTodoListName(todoListInput.getTodoListName());
                    return todoListRepository.save(tl);
                });

        todoList.ifPresent(todoList1 -> userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyFollowersAboutUpdatingTodolist(user, todoList1)));

        return todoList;
    }

    public void deleteTodoList(Long todoListId, Long userId) {

        Optional<TodoList> todoList = todoListRepository.findById(todoListId);

        if (todoList.isPresent()) {
            todoListRepository.deleteById(todoListId);
            userService.getUserById(userId)
                    .ifPresent(user -> notificationService.notifyFollowersAboutDeletingTodolist(user, todoList.get()));
        }
    }

    public ApiResponse shareTodoList(String targetUsername, Long sharedTodoListId, Long userId) {

        Optional<User> targetUser = userService.getUserByUsername(targetUsername);
        Optional<TodoList> sharedTodoList = todoListRepository.findById(sharedTodoListId);

        if (targetUser.isPresent() && sharedTodoList.isPresent()) {
            Share share = new Share(targetUser.get().getId(), sharedTodoList.get());
            shareService.addShare(share);

            userService.getUserById(userId)
                    .ifPresent(user -> {
                        notificationService.notifyAboutSharingTodolist(user, targetUser.get(), sharedTodoList.get());
                    });
        }
        return new ApiResponse(true, "You shared your todoList to " + targetUsername + "!");
    }
}
