package com.list.todo.repositories;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends PagingAndSortingRepository<Share, Long> {
	List<Share> findBySharedUserId(Long userId);

    void deleteBySharedTodoListId(Long todoListId);

    boolean existsBySharedTodoListId(Long sharedTodoListId);

    Page<Share> findBySharedUserId(Long userId, Pageable pageable);
}
