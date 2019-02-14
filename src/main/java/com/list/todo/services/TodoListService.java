package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.payload.ApiResponse;
import com.list.todo.payload.InputTodoList;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TodoListService {
	
    private final TodoListRepository repository;

    private FollowerService followerService;
    private UserService userService;
    private ShareService shareService;

    public Optional<TodoList> getTodoListById(Long todoListId){
        return repository.findById(todoListId);
    }

    public List<TodoList> getTodoListsByUser(Long userId){
        return repository.findTodoListsByUserOwnerId(userId);
    }

    public TodoList addTodoList(InputTodoList inputTodoList){
        UserPrincipal currentUser = getCurrentUser();

        Long userId = currentUser.getId();

        TodoList todoList = new TodoList();
        todoList.setUserOwnerId(userId);
        todoList.setTodoListName(inputTodoList.getTodoListName());
        repository.save(todoList);

        followerService.notifyFollowersAboutAddTodoList(currentUser, inputTodoList);

        return todoList;
    }
    
    public TodoList updateTodoList(Long todoListId, InputTodoList inputTodoList) {
        UserPrincipal currentUser = getCurrentUser();

        TodoList todoList = repository.findById(todoListId).orElse(null);

        if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
            todoList.setTodoListName(inputTodoList.getTodoListName());
            todoList = repository.save(todoList);
            followerService.notifyFollowersAboutUpdatingTodoList(currentUser, todoList);
        }
    	return todoList;
    }
    
    public boolean deleteTodoList(Long todoListId) {
        UserPrincipal currentUser = getCurrentUser();

        TodoList todoList = repository.findById(todoListId).orElse(null);

        boolean isSuccess = false;

        if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
            repository.deleteById(todoListId);
            followerService.notifyFollowersAboutDeletingTodoList(currentUser, todoList);
            isSuccess = true;
        }
    	return isSuccess;
    }

    public ApiResponse shareTodoList(String sharedUsername, Long sharedTodoListId){
        UserPrincipal currentUser = getCurrentUser();

        User sharedUser = userService.getUserByUsername(sharedUsername);
        TodoList sharedTodoList = repository.findById(sharedTodoListId).orElse(null);

        ApiResponse apiResponse = new ApiResponse(false,
                "You can't share this todoList to " + sharedUsername + "!");

        if(sharedUser != null && sharedTodoList != null && sharedTodoList.getUserOwnerId().equals(currentUser.getId())
                && !currentUser.getUsername().equals(sharedUsername)) {

            Share share = new Share(sharedUser.getId(), sharedTodoList);
            shareService.addShare(share);
            shareService.sendNotificationAboutShareTodoList(sharedUser, currentUser, sharedTodoList);
            followerService.notifyFollowersAboutSharingTodoList(currentUser, sharedTodoList, sharedUser);

            apiResponse = new ApiResponse(true, "You shared your todoList to " + sharedUsername + "!");
        }
        return apiResponse;
    }

    private UserPrincipal getCurrentUser(){
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
