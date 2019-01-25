package com.list.todo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	public List<Task> findTasksByTodoListId(Long todoListId);
}
