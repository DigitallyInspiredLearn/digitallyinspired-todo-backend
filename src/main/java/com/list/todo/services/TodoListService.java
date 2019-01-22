package com.list.todo.services;

import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TodoListRepository;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TodoListService {
	
    private final TodoListRepository repository;

    @Autowired
    public TodoListService(TodoListRepository repository) {
        this.repository = repository;
    }

    public Set<TodoList> getAllTodoListsByUser(Long userId){
        return repository.findTodoListsByUserOwnerId(userId);
    }
    
    public TodoList getTodoList(Long id){
        return repository.findById(id).get();
    }

    public void addTodoList(TodoList todoList){
        repository.save(todoList);
    }
    
    public void updateTodoList(TodoList todoList) {
    	repository.save(todoList);
    }
    
    public void deleteTodoList(Long id) {
    	repository.deleteById(id);
    }
}
