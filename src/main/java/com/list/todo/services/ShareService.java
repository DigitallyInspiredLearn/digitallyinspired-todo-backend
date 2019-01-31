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

@Service
@AllArgsConstructor
public class ShareService {

	private ShareRepository sharesRepository;

	private final EmailService emailService;

	public List<TodoList> getSharedTodoListsByUser(Long userId) {
		List<Share> shares = sharesRepository.findBySharedUserId(userId);
		List<TodoList> sharedTodoLists = new ArrayList<>();
		shares
			.forEach(share -> sharedTodoLists.add(share.getSharedTodoList()));
		
		return sharedTodoLists;
	}
	
	public void addShare(Share shares) {
		sharesRepository.save(shares);
	}

	public void sendNotificationAboutShareTodoList(User sharedUser, UserPrincipal senderUser, TodoList sharedTodoList){
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
}
