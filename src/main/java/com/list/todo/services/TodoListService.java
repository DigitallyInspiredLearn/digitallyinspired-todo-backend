package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.entity.User;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Iterable<TodoList> getTodoListsByUser(String createdBy, TodoListStatus todoListStatus, Pageable pageable) {
        Iterable<TodoList> todoLists;

        if (todoListStatus.equals(TodoListStatus.ALL)) {
            todoLists = todoListRepository.findByCreatedBy(createdBy, pageable);
        } else {
            todoLists = todoListRepository.findByCreatedByAndTodoListStatus(createdBy, todoListStatus, pageable);
        }

        return todoLists;
    }

    public Long countTodolistsByCreatedBy(String createdBy) {
        return todoListRepository.countByCreatedBy(createdBy);
    }

    public Optional<TodoList> addTodoList(TodoListInput todoListInput, Long userId) {

        TodoList todoList = TodoList.builder()
                .todoListName(todoListInput.getTodoListName())
                .todoListStatus(TodoListStatus.ACTIVE)
                .build();

        Optional<TodoList> newTodoList = Optional.of(todoListRepository.save(todoList));

        todoListInput.getTasks().forEach(task -> task.setTodoList(newTodoList.get()));
        newTodoList.get().setTasks(todoListInput.getTasks());


        Optional<TodoList> updatedTodoList = Optional.of(todoListRepository.save(todoList));

        userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyAboutAddingTodoList(user, todoList));

        return updatedTodoList;
    }

    public Optional<TodoList> updateTodoList(Long todoListId, TodoListInput todoListInput, Long userId) {

        Optional<TodoList> todoList = todoListRepository.findById(todoListId)
                .map(tl -> {
                    tl.setTodoListName(todoListInput.getTodoListName());
                    return todoListRepository.save(tl);
                });

        todoList.ifPresent(todoList1 -> userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyAboutUpdatingTodoList(user, todoList1)));

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
                    .ifPresent(user -> notificationService.notifyAboutDeletingTodoList(user, todoList.get()));
        });
    }

    public Optional<TodoList> changeTodoListStatus(Long todoListId, TodoListStatus todoListStatus) {
        return todoListRepository.findById(todoListId)
                .map(tl -> {
                    tl.setTodoListStatus(todoListStatus);
                    return todoListRepository.save(tl);
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
                        notificationService.notifyAboutSharingTodoList(user, targetUser.get(), sharedTodoList.get());
                    });
            apiResponse = new ApiResponse(true, "You shared your todoList to " + targetUsername + "!");
        }
        return apiResponse;
    }

    public Iterable<TodoList> searchTodoListByName(String todoListName, String createdBy, Pageable pageable){
        return todoListRepository.findByTodoListNameLikeAndCreatedByEqualsAndTodoListStatus(todoListName, createdBy, TodoListStatus.ACTIVE, pageable);
    }
}
