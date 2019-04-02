package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TaggedTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaggedTaskService {

    private final TaggedTaskRepository taggedTaskRepository;

    private final TaskService taskService;
    private final TagService tagService;

    public Optional<TaggedTask> addTagToTask(Long taskId, Long tagId) {
        Task task = taskService.getTaskById(taskId).orElse(null);
        Tag tag = tagService.getTagById(tagId).orElse(null);

        Optional<TaggedTask> taggedTask = Optional.empty();

        if (task != null && tag != null) {
            taggedTask = Optional.of(taggedTaskRepository.save(new TaggedTask(task.getId(), tag)));
        }

        return taggedTask;
    }

    public List<TaggedTask> getTaggedTaskByTag(Tag tag) {
        return taggedTaskRepository.findByTag(tag);
    }

    public void deleteTaggedTask(TaggedTask taggedTask) {
        taggedTaskRepository.delete(taggedTask);
    }


    public Set<TodoList> getTodoListsByTags(List<Long> tagsId, Long currentUserId) {
        Set<Task> tasksByTags = getTasksByTags(tagsId, currentUserId);

        return tasksByTags
                .stream()
                .map(Task::getTodoList)
                .collect(Collectors.toSet());
    }

    private Set<Task> getTasksByTags(List<Long> tagsIds, Long currentUserId) {
        Set<Task> tasksByTags = new HashSet<>();

        for (Long tagId : tagsIds) {
            Optional<Tag> tag = tagService.getTagById(tagId);

            if (tag.isPresent()) {
                if (tag.get().getOwnerId().equals(currentUserId)) {

                    tasksByTags.addAll(taggedTaskRepository.findByTag(tag.get())
                            .stream()
                            .map(taggedTask -> taskService.getTaskById(taggedTask.getTaskId()).orElse(null))
                            .collect(Collectors.toSet()));
                }
            }
        }

        return tasksByTags;
    }

}
