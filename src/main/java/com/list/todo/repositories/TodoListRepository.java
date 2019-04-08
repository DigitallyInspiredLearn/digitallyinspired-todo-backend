package com.list.todo.repositories;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends PagingAndSortingRepository<TodoList, Long> {
	Long countByCreatedBy(String createdBy);
	List<TodoList> findByCreatedBy(String createdBy);
	Page<TodoList> findByCreatedBy(String createdBy, Pageable pageable);
	Page<TodoList> findByCreatedByAndTodoListStatus(String createdBy, TodoListStatus todoListStatus, Pageable pageable);
	Page<TodoList> findByTodoListNameLikeAndCreatedByEqualsAndTodoListStatus(String todoListName, String createdBy, TodoListStatus todoListStatus, Pageable pageable);
}
