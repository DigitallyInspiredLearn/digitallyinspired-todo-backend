package com.list.todo.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TodoListRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TodoListService {
	
    private final TodoListRepository repository;

    public List<TodoList> getTodoListsByUser(Long userId){
        return repository.findTodoListsByUserOwnerId(userId);
    }

    public void addTodoList(TodoList todoList){
        repository.save(todoList);
    }
    
    public void updateTodoList(TodoList todoList) {
    	repository.save(todoList);
    }
    
    public void deleteTodoList(TodoList todoList) {
    	repository.delete(todoList);
    }
}
