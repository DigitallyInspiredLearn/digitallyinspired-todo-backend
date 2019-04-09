package com.list.todo.repositories;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TagTaskKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagTaskKeyRepository extends JpaRepository<TagTaskKey, Long> {
    List<TagTaskKey> findByTag(Tag tag);
    Optional<TagTaskKey> findByTaskIdAndTag(Long taskId, Tag tag);
    List<TagTaskKey> findByTaskId(Long taskId);
}
