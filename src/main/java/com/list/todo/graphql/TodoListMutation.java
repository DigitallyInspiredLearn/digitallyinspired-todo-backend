package com.list.todo.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.list.todo.entity.*;
import com.list.todo.payload.ApiResponse;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.repositories.UserRepository;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.FollowerService;
import com.list.todo.services.ShareService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class TodoListMutation implements GraphQLMutationResolver {

    private UserRepository userRepository;
    private TodoListRepository todoListRepository;

    private ShareService shareService;
    private FollowerService followerService;

    public TodoList addTodoList(String todoListName) {

        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TodoList todoList = new TodoList();
        todoList.setTodoListName(todoListName);
        todoList.setUserOwnerId(currentUser.getId());

        followerService.notifyFollowersAboutAddTodoList(currentUser, todoList);

        return todoListRepository.save(todoList);
    }

    public TodoList updateTodoList(Long todoListId, String todoListName) {

        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TodoList todoList = todoListRepository.findById(todoListId).orElse(null);

        if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
            todoList.setTodoListName(todoListName);
            todoList = todoListRepository.save(todoList);
            followerService.notifyFollowersAboutUpdatingTodoList(currentUser, todoList);

        }


        return todoList;
    }

    public boolean deleteTodoList(Long todoListId) {

        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TodoList todoList = todoListRepository.findById(todoListId).orElse(null);
        boolean isSuccess = false;

        if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
            todoListRepository.deleteById(todoListId);
            followerService.notifyFollowersAboutDeletingTodoList(currentUser, todoList);
            isSuccess = true;
        }

        return isSuccess;
    }

    public ApiResponse shareTodoListToUser(String sharedUsername, Long sharedTodoListId) {

        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User sharedUser = userRepository.findByUsername(sharedUsername).orElse(null);
        TodoList sharedTodoList = todoListRepository.findById(sharedTodoListId).orElse(null);

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

}
