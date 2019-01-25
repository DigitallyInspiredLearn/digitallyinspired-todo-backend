package com.list.todo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.ShareRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ShareService {

	private ShareRepository sharesRepository;

	public List<TodoList> getSharedTodoListsByUser(Long userId) {
		List<Share> shares = sharesRepository.findBySharedUserId(userId);
		List<TodoList> sharedTodoLists = new ArrayList<>();
		shares
			.stream()
			.forEach(share -> sharedTodoLists.add(share.getSharedTodoList()));
		
		return sharedTodoLists;
	}
	
	public void addShare(Share shares) {
		sharesRepository.save(shares);
	}
}
