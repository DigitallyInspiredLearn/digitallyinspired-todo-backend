package com.list.todo.repositories;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.TodoList;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {
	public Set<TodoList> findTodoListsByUserOwnerId(Long userId);
}
