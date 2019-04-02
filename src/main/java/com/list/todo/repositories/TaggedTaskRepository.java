package com.list.todo.repositories;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaggedTaskRepository extends JpaRepository<TaggedTask, Long> {
    List<TaggedTask> findByTag(Tag tag);
    Optional<TaggedTask> findByTaskIdAndTag(Long taskId, Tag tag);
}