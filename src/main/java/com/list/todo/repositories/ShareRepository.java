package com.list.todo.repositories;

import com.list.todo.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
	List<Share> findBySharedUserId(Long userId);
}
