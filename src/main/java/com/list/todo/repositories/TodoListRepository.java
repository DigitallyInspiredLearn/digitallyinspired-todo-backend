package com.list.todo.repositories;

import com.list.todo.entity.TodoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends PagingAndSortingRepository<TodoList, Long> {
	Page<TodoList> findTodoListsByCreatedBy(String createdBy, Pageable pageable);
	List<TodoList> findTodoListsByCreatedBy(String createdBy);
	Page<TodoList> findTodoListByTodoListNameLikeAndCreatedByEquals(String todoListName, String createdBy, Pageable pageable);
	Long countByCreatedBy(String createdBy);
}
