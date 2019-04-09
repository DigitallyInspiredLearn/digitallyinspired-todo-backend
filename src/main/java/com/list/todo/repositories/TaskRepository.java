package com.list.todo.repositories;

import java.util.List;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findTasksByTodoListIdOrderByPriority(Long todoListId);
	Long countByCreatedBy(String createdBy);
	Long countByCreatedByAndIsComplete(String createdBy, Boolean isComplete);
}
