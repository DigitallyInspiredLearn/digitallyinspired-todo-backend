package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;

    private final UserService userService;
    private final ShareService shareService;
    private final NotificationService notificationService;

    public Optional<TodoList> getTodoListById(Long todoListId) {
        return todoListRepository.findById(todoListId);
    }

    public Iterable<TodoList> getTodoListsByUser(Long userId) {
        return todoListRepository.findTodoListsByUserOwnerId(userId);
    }

    public Optional<TodoList> addTodoList(TodoListInput todoListInput, Long userId) {

        TodoList todoList = TodoList.builder()
                .todoListName(todoListInput.getTodoListName())
                .userOwnerId(userId)
                .build();

        Optional<TodoList> newTodoList = Optional.of(todoListRepository.save(todoList));
        Optional<TodoList> updatedTodoList = newTodoList;

        todoListInput.getTasks().forEach(task -> task.setTodoList(newTodoList.get()));
        newTodoList.get().setTasks(todoListInput.getTasks());


        Optional<TodoList> updatedTodoList = Optional.of(todoListRepository.save(todoList));

        userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyFollowersAboutAddingTodolist(user, todoList));

        return updatedTodoList;
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

    public void deleteTodoList(Long todoListId, Long currentUserId) {

        Optional<TodoList> todoList = todoListRepository.findById(todoListId);

        todoList.ifPresent(tList -> {
            if (shareService.isSharedTodoList(todoListId)) {
                shareService.deleteShareBySharedTodoListId(tList.getId());
            } else {
                todoListRepository.deleteById(tList.getId());
            }

            userService.getUserById(currentUserId)
                    .ifPresent(user -> notificationService.notifyFollowersAboutDeletingTodolist(user, todoList.get()));
        });
    }

    public ApiResponse shareTodoList(String targetUsername, Long sharedTodoListId, Long userId) {

        ApiResponse apiResponse = new ApiResponse(false, "Something went wrong!");
        Optional<User> targetUser = userService.getUserByUsername(targetUsername);
        Optional<TodoList> sharedTodoList = todoListRepository.findById(sharedTodoListId);

        if (targetUser.isPresent() && sharedTodoList.isPresent()) {
            Share share = new Share(targetUser.get().getId(), sharedTodoList.get());
            shareService.addShare(share);

            userService.getUserById(userId)
                    .ifPresent(user -> {
                        notificationService.notifyAboutSharingTodolist(user, targetUser.get(), sharedTodoList.get());
                    });
            apiResponse = new ApiResponse(true, "You shared your todoList to " + targetUsername + "!");
        }
        return apiResponse;
    }
}
