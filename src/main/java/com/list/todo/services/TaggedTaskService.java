package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import com.list.todo.entity.Task;
import com.list.todo.repositories.TaggedTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaggedTaskService {

    private final TaggedTaskRepository taggedTaskRepository;

    private final TaskService taskService;
    private final TagService tagService;

    public boolean addTagToTask(Long taskId, Long tagId) {
        Task task = taskService.getTaskById(taskId).orElse(null);
        Tag tag = tagService.getTagById(tagId).orElse(null);
        boolean isSuccess = false;

        if (task != null && tag != null) {
            taggedTaskRepository.save(new TaggedTask(task.getId(), tag));
            isSuccess = true;
        }

        return isSuccess;
    }

    public Set<Task> getTasksByTags(List<Tag> tags) {
        Set<Task> tasksByTags = new HashSet<>();

        for (Tag tag : tags){
             tasksByTags.addAll(tasksByTags = taggedTaskRepository.findByTag(tag)
                     .stream()
                     .map(taggedTask -> taskService.getTaskById(taggedTask.getTaskId()).orElse(null))
                     .collect(Collectors.toSet()));
         }

        return tasksByTags;
    }

}
