package com.list.todo.controllers;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TagInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TagService;
import com.list.todo.services.TaggedTaskService;
import com.list.todo.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TagController {

    private final TagService tagService;
    private final TaggedTaskService taggedTaskService;
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Iterable<Tag>> getMyTags(@AuthenticationPrincipal UserPrincipal currentUser){
        Iterable<Tag> myTags = tagService.getTagsByOwnerId(currentUser.getId());

        return new ResponseEntity<>(myTags, HttpStatus.OK);
    }

    @GetMapping("/myTaggedTask")
    public ResponseEntity<Iterable<TaggedTask>> getMyTagsWithTodoListId(@AuthenticationPrincipal UserPrincipal currentUser,
                                                                        Pageable pageable) {
        Iterable<TaggedTask> myTaggedTask = taggedTaskService.getMyTaggedTask(currentUser, pageable);

        return new ResponseEntity<>(myTaggedTask, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Optional<Tag>> addTag(@RequestBody TagInput tagInput,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        Optional<Tag> tag = tagService.addTag(tagInput, currentUser.getId());

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Tag>> updateTag(@RequestBody TagInput tagInput,
                                                   @PathVariable("id") Long tagId,
                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Optional<Tag>> responseEntity;
        Optional<Tag> tag = tagService.getTagById(tagId);

        if (tag.isPresent()) {
            if (!tag.get().getOwnerId().equals(currentUser.getId())){
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                Optional<Tag> updatedTag = tagService.updateTag(tag.get().getId(), tagInput);
                responseEntity = new ResponseEntity<>(updatedTag, HttpStatus.OK);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable("id") Long tagId,
                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Void> responseEntity;
        Optional<Tag> tag = tagService.getTagById(tagId);

        if (tag.isPresent()) {
            if (!tag.get().getOwnerId().equals(currentUser.getId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                tagService.deleteTag(tag.get().getId());
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }

    @DeleteMapping("/removeTagFromTask/{id}")
    public ResponseEntity<Void> removeTagFromTheTask(@PathVariable("id") Long tagId,
                                                     @RequestParam("taskId") Long taskId,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        ResponseEntity<Void> responseEntity;
        Optional<Tag> tag = tagService.getTagById(tagId);

        if (tag.isPresent()) {
            if (!tag.get().getOwnerId().equals(currentUser.getId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                taggedTaskService.deleteTaggedTask(taskId, tag.get());
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return responseEntity;

    }

    @PostMapping("/{id}")
    public ResponseEntity<Optional<TaggedTask>> addTagToTask(@PathVariable("id") Long tagId,
                                                             @RequestParam("taskId") Long taskId,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {

        ResponseEntity<Optional<TaggedTask>> responseEntity;
        Optional<Tag> tag = tagService.getTagById(tagId);
        Optional<Task> task = taskService.getTaskById(taskId);

        if (task.isPresent() && tag.isPresent()){
            TodoList todoList = task.get().getTodoList();

            if (todoList == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (!todoList.getCreatedBy().equals(currentUser.getUsername()) ||
                    !tag.get().getOwnerId().equals(currentUser.getId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                Optional<TaggedTask> taggedTask = taggedTaskService.addTagToTask(taskId, tagId);
                responseEntity = new ResponseEntity<>(taggedTask, HttpStatus.OK);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
}
