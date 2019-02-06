package com.list.todo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.Share;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
	List<Share> findBySharedUserId(Long userId);
}
