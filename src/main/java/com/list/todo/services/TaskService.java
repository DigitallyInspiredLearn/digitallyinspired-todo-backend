package com.list.todo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.list.todo.entity.Task;
import com.list.todo.repositories.TaskRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	public List<Task> getAllTasksOnTodoList(Long todoListId) {
		return taskRepository.findTasksByTodoListId(todoListId);
	}

	public void addTask(Task task) {
		taskRepository.save(task);
	}

	public void updateTask(Task task) {
		taskRepository.save(task);
	}

	public void deleteTask(Task task) {
		taskRepository.delete(task);
	}

}
