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

    // TODO: перенести поля на final
    private ShareRepository sharesRepository;

    private final EmailService emailService;

    public Iterable<TodoList> getSharedTodoListsByUser(Long userId) {

        return sharesRepository.findBySharedUserId(userId)
                .stream()
                .map(Share::getSharedTodoList)
                .collect(Collectors.toList());
    }


    public void addShare(Share shares) {
        sharesRepository.save(shares);
    }

    // TODO: вынести сообщения в файл .properties
    public void sendNotificationAboutShareTodoList(User sharedUser, User senderUser, TodoList sharedTodoList) {
        // TODO: вынести 'http://localhost:8080' в файл .properties
        String link = "http://localhost:8080/api/todolists/shared";
        String subject = "You have been new shared todo list!";

        String message = String.format(
                "Hi, %s!\n" +
                        "User %s shared with you TodoList: \"%s\". " +
                        "Follow the link to view: %s",
                sharedUser.getName(),
                senderUser.getName(),
                sharedTodoList.getTodoListName(),
                link
        );

        emailService.sendEmail(sharedUser.getEmail(), subject, message);
    }

    public boolean isSharedTodoListToUser(TodoList sharedTodoList, User sharedUser) {

        return sharesRepository.findBySharedUserId(sharedUser.getId())
                .stream()
                .anyMatch(share -> share.getSharedTodoList().hashCode() == sharedTodoList.hashCode());
    }
}
