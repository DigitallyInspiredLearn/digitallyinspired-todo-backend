package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.repositories.ShareRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShareService {

    private final ShareRepository sharesRepository;

    public Iterable<TodoList> getSharedTodoListsByUser(Long userId) {

        return sharesRepository.findBySharedUserId(userId)
                .stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
    }


    public void addShare(Share shares) {
        sharesRepository.save(shares);
    }

    public boolean isSharedTodoListToUser(TodoList sharedTodoList, User sharedUser) {

        return sharesRepository.findBySharedUserId(sharedUser.getId())
                .stream()
                .anyMatch(share -> share.getSharedTodoList().hashCode() == sharedTodoList.hashCode());
    }
}
