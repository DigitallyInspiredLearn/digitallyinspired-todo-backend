package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;
import com.list.todo.repositories.ShareRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public Iterable<TodoList> getSharedTodoListsByUser(Long userId, Pageable pageable) {
        Page<Share> sharesPage = sharesRepository.findBySharedUserId(userId, pageable);
        List<TodoList> sharedTodoLists = sharesPage.getContent()
                .stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
        return new PageImpl<>(sharedTodoLists, pageable, sharesPage.getTotalElements());
    }


    public void addShare(Share shares) {
        sharesRepository.save(shares);
    }

    public boolean isSharedTodoListToUser(TodoList sharedTodoList, Long sharedUserId) {
        return sharesRepository.findBySharedUserId(sharedUserId)
                .stream()
                .anyMatch(share -> share.getSharedTodoList().hashCode() == sharedTodoList.hashCode());
    }

    public boolean isSharedTodoList(Long sharedTodoListId) {
        return sharesRepository.existsBySharedTodoListId(sharedTodoListId);
    }

    @Transactional
    public void deleteShareBySharedTodoListId(Long todoListId) {
        sharesRepository.deleteBySharedTodoListId(todoListId);
    }

}
