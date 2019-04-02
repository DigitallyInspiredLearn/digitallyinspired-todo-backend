package com.list.todo.repositories;

import com.list.todo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> getByOwnerId(Long ownerId);
}
