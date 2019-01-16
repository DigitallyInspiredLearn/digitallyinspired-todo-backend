package com.list.todo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.list.todo.entity.Task;
import com.list.todo.repositories.TaskRepository;

@Service
public class TaskService {

	@Autowired
	private TaskRepository taskRepository;
	
	public List<Task> getAllTasksOnProject(Long projectId) {
		return taskRepository.findTasksByProjectId(projectId);
	}

	public Task getTask(Long id) {
		return taskRepository.findById(id).get();
	}

	public void addTask(Task task) {
		taskRepository.save(task);
	}

	public void updateTask(Task task) {
		taskRepository.save(task);
	}

	public void deleteTask(Long id) {
		taskRepository.deleteById(id);
	}

}
