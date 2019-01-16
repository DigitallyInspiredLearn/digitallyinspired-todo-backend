package com.list.todo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.list.todo.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	public List<Project> findProjectsByUserOwnerId(Long userId);
}
