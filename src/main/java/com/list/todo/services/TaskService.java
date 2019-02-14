package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.InputTask;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	private TodoListService todoListService;

	public List<Task> getAllTasksOnTodoList(Long todoListId) {
		return taskRepository.findTasksByTodoListId(todoListId);
	}

	public Task addTask(InputTask inputTask) {

		UserPrincipal currentUser = getCurrentUser();
		TodoList todoList = todoListService.getTodoListById(inputTask.getTodoListId()).orElse(null);
		Task newTask = new Task();

		if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
			newTask.setBody(inputTask.getBody());
			newTask.setIsComplete(false);
			newTask.setTodoList(todoList);
			newTask = taskRepository.save(newTask);
		}

		return newTask;
	}

	public Task updateTask(Long currentTaskId, InputTask inputTask) {

		UserPrincipal currentUser = getCurrentUser();
		Task currentTask = taskRepository.findById(currentTaskId).orElse(null);

		TodoList currentTodoList;

		if (currentTask != null) {
			currentTodoList = currentTask.getTodoList();

			if (currentTodoList != null && currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
				currentTask.setBody(inputTask.getBody());
				currentTask.setIsComplete(inputTask.getIsComplete());

				currentTask = taskRepository.save(currentTask);
			}
		}

		return currentTask;
	}

	public boolean deleteTask(Long taskId) {

		UserPrincipal currentUser = getCurrentUser();
		Task task = taskRepository.findById(taskId).orElse(null);
		TodoList currentTodoList;
		boolean isSuccess = false;

		if (task != null) {

			currentTodoList = task.getTodoList();

			if (currentTodoList != null && currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
				taskRepository.deleteById(taskId);
				isSuccess = true;
			}
		}
		return isSuccess;
	}

	private UserPrincipal getCurrentUser(){
		return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

}
