package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.TodoListInput;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;

    private final UserService userService;
    private final ShareService shareService;
    private final TagTaskKeyService tagTaskKeyService;
    private final NotificationService notificationService;

    public Optional<TodoList> getTodoListById(Long todoListId) {
        return todoListRepository.findById(todoListId);
    }


    public Iterable<TodoList> getTodoListsByUser(UserPrincipal currentUser, TodoListStatus todoListStatus, Pageable pageable, List<Long> tagsId) {

        List<Task> tasks = new ArrayList<>(tagTaskKeyService.getTasksByTags(tagsId, currentUser.getId()));
        Page<TodoList> todoLists;

        if (todoListStatus == TodoListStatus.ALL){
            todoLists = todoListRepository.findDistinctByCreatedByAndTasksIn(currentUser.getUsername(), pageable, tasks);
        }
        else {
            todoLists = todoListRepository.findDistinctByCreatedByAndTodoListStatusAndTasksIn(currentUser.getUsername(), todoListStatus, pageable, tasks);
        }

        return todoLists;
    }

    public Long countTodoListsByCreatedBy(String createdBy) {
        return todoListRepository.countByCreatedBy(createdBy);
    }

    public Optional<TodoList> addTodoList(TodoListInput todoListInput, Long userId) {

        TodoList todoList = TodoList.builder()
                .todoListName(todoListInput.getTodoListName())
                .todoListStatus(TodoListStatus.ACTIVE)
                .comment(todoListInput.getComment())
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
        AtomicReference<TodoList> updatedTodoList = new AtomicReference<>();

        todoListRepository.findById(todoListId)
                .map(todoList -> {
                    todoList.setTodoListName(todoListInput.getTodoListName());
                    todoList.setComment(todoListInput.getComment());
                    updatedTodoList.set(todoListRepository.save(todoList));
                    return updatedTodoList.get();
                }).ifPresent(todoList -> userService.getUserById(userId)
                .ifPresent(user -> notificationService.notifyAboutUpdatingTodoList(user, todoList)));

        return Optional.of(updatedTodoList.get());
    }

    public void deleteTodoList(Long todoListId, Long currentUserId) {
        todoListRepository.findById(todoListId).ifPresent(tList -> {
            if (shareService.isSharedTodoList(todoListId)) {
                shareService.deleteShareBySharedTodoListId(tList.getId());
            } else {
                tList.getTasks().forEach(task -> tagTaskKeyService.deleteTaggedTask(task.getId()));
                todoListRepository.deleteById(tList.getId());
            }
            userService.getUserById(currentUserId)
                    .ifPresent(user -> notificationService.notifyAboutDeletingTodoList(user, tList));
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

        AtomicReference<ApiResponse> apiResponse = new AtomicReference<>(new ApiResponse(false, "Something went wrong!"));

        userService.getUserByUsername(targetUsername).ifPresent(targetUser ->
                todoListRepository.findById(sharedTodoListId).ifPresent(sharedTodoList -> {
                    shareService.addShare(new Share(targetUser.getId(), sharedTodoList));
                    userService.getUserById(userId)
                            .ifPresent(user -> notificationService.notifyAboutSharingTodoList(user, targetUser, sharedTodoList));
                    apiResponse.set(new ApiResponse(true, "You shared your todoList to " + targetUsername + "!"));
                }));

        return apiResponse.get();
    }

    public Iterable<TodoList> searchTodoListByName(String todoListName, String createdBy, Pageable pageable) {
        return todoListRepository.
                findByTodoListNameLikeAndCreatedByEqualsAndTodoListStatus(todoListName, createdBy, TodoListStatus.ACTIVE, pageable);
    }
}
