package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	public List<Task> getAllTasksOnTodoList(TodoList todoList) {
		return taskRepository.findTasksByTodoListId(todoList.getId());
	}

	public Task getTask(Long id) {
		Optional<Task> task = taskRepository.findById(id);
		return task.orElse(null);
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
