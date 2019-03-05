package com.list.todo.repositories;

import com.list.todo.entity.TodoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends PagingAndSortingRepository<TodoList, Long> {
	Page<TodoList> findTodoListsByCreatedBy(Long userId, Pageable pageable);
	List<TodoList> findTodoListsByCreatedBy(Long userId);
	Page<TodoList> findTodoListByTodoListNameLikeAndCreatedByEquals(String todoListName, Long userId, Pageable pageable);
}
