package com.list.todo.repositories;

import com.list.todo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findTasksByTodoListIdOrderByPriority(Long todoListId);
	Long countByCreatedBy(String createdBy);
	Long countByCreatedByAndIsComplete(String createdBy, Boolean isComplete);
}
