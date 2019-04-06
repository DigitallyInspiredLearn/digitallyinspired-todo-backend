package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.TaggedTaskRepository;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final TaskRepository taskRepository;
    private final TodoListRepository todoListRepository;

    private final TagService tagService;

    public Optional<TaggedTask> addTagToTask(Long taskId, Long tagId) {
        Task task = taskRepository.getOne(taskId);
        Tag tag = tagService.getTagById(tagId).orElse(null);

        Optional<TaggedTask> taggedTask = Optional.empty();

        if (task != null && tag != null) {
            taggedTask = Optional.of(taggedTaskRepository.save(new TaggedTask(task.getId(), tag)));
        }

        return taggedTask;
    }

    public Set<TaggedTask> getMyTaggedTask(UserPrincipal currentUser, Pageable pageable) {
        Set<TaggedTask> myTaggedTask = new HashSet<>();

        Iterable<TodoList> todoListsByCreatedBy = todoListRepository.findTodoListsByCreatedBy(currentUser.getUsername(), pageable);

        todoListsByCreatedBy
                .forEach(todoList -> todoList.getTasks()
                .forEach(task -> myTaggedTask.add(taggedTaskRepository.findByTaskId(task.getId()))));

        return myTaggedTask;
    }

    public Set<Task> getTasksByTags(List<Long> tagsIds, Long currentUserId) {
        Set<Task> tasksByTags = new HashSet<>();

        tagsIds.forEach(tagId -> {
            Optional<Tag> tag = tagService.getTagById(tagId);

            if (tag.isPresent()) {
                if (tag.get().getOwnerId().equals(currentUserId)) {

                    tasksByTags.addAll(taggedTaskRepository.findByTag(tag.get())
                            .stream()
                            .map(taggedTask -> taskRepository.getOne(taggedTask.getTaskId()))
                            .collect(Collectors.toSet()));
                }
            }
        });

        return tasksByTags;
    }

    public List<TaggedTask> getTaggedTaskByTag(Tag tag) {
        return taggedTaskRepository.findByTag(tag);
    }

    public void deleteTaggedTask(Long taskId, Tag tag) {

        TaggedTask taggedTask = taggedTaskRepository.findByTaskIdAndTag(taskId, tag);

        taggedTaskRepository.delete(taggedTask);
    }

}
