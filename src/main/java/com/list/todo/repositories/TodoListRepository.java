package com.list.todo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.TodoList;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {
	List<TodoList> findTodoListsByCreatedBy(Long userId);
}
