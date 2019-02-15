package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	private TodoListService todoListService;
	private UserService userService;

	public Iterable<Task> getAllTasksOnTodoList(Long todoListId) {
		UserPrincipal currentUser = userService.getCurrentUser();
		TodoList todoList = todoListService.getTodoListById(todoListId).orElse(null);
		Iterable<Task> tasks = null;

		if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
			tasks = taskRepository.findTasksByTodoListId(todoListId);
		}

		return tasks;
	}

	public Task addTask(TaskInput taskInput, UserPrincipal currentUser) {

		TodoList todoList = todoListService.getTodoListById(taskInput.getTodoListId()).orElse(null);
		Task newTask = new Task();

		if (todoList != null && todoList.getUserOwnerId().equals(currentUser.getId())) {
			newTask.setBody(taskInput.getBody());
			newTask.setIsComplete(false);
			newTask.setTodoList(todoList);
			newTask = taskRepository.save(newTask);
		}

		return newTask;
	}

	public Task updateTask(Long currentTaskId, TaskInput taskInput, UserPrincipal currentUser) {

		Task currentTask = taskRepository.findById(currentTaskId).orElse(null);

		TodoList currentTodoList;

		if (currentTask != null) {
			currentTodoList = currentTask.getTodoList();

			if (currentTodoList != null && currentTodoList.getUserOwnerId().equals(currentUser.getId())) {
				currentTask.setBody(taskInput.getBody());
				currentTask.setIsComplete(taskInput.getIsComplete());

				currentTask = taskRepository.save(currentTask);
			}
		}

		return currentTask;
	}

	public boolean deleteTask(Long taskId, UserPrincipal currentUser) {

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

}
