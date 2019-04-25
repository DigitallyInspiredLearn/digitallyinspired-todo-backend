package com.list.todo.repositories;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends PagingAndSortingRepository<TodoList, Long> {
    List<TodoList> findByCreatedBy(String createdBy);

    Page<TodoList> findByCreatedBy(String createdBy, Pageable pageable);

    Page<TodoList> findByCreatedByAndTodoListNameLike(String createdBy, Pageable pageable, String todoListName);

    Page<TodoList> findByCreatedByAndTodoListStatus(String createdBy, TodoListStatus todoListStatus, Pageable pageable);

    Page<TodoList> findByCreatedByAndTodoListStatusAndTodoListNameLike(String createdBy, TodoListStatus todoListStatus,
                                                                       Pageable pageable, String todoListName);

    Page<TodoList> findDistinctByCreatedByAndTodoListStatusAndTasksInAndTodoListNameLike(String createdBy,
                                                                                         TodoListStatus todoListStatus,
                                                                                         Pageable pageable,
                                                                                         List<Task> tasks,
                                                                                         String todoListName);

    Page<TodoList> findDistinctByCreatedByAndTasksInAndTodoListNameLike(String createdBy, Pageable pageable,
                                                                        List<Task> tasks, String todoListName);

    Page<TodoList> findByTodoListNameLikeAndCreatedByAndTodoListStatus(String todoListName, String createdBy,
                                                                       TodoListStatus todoListStatus, Pageable pageable);

    Long countByCreatedBy(String createdBy);
}
