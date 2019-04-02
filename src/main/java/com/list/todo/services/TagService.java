package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.payload.TagInput;
import com.list.todo.repositories.TagRepository;
import com.list.todo.repositories.TaggedTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaggedTaskRepository taggedTaskRepository;

    public Optional<Tag> getTagById(Long tagId) {
        return tagRepository.findById(tagId);
    }

    public Iterable<Tag> getTagsByOwnerId(Long ownerId) {
        return tagRepository.getByOwnerId(ownerId);
    }

    public Optional<Tag> addTag(TagInput tagInput, Long currentUserId) {
        Tag newTag = Tag.builder()
                .nameTag(tagInput.getTagName())
                .ownerId(currentUserId)
                .build();

        return Optional.of(tagRepository.save(newTag));
    }

    public Optional<Tag> updateTag(Long currentTagId, TagInput tagInput) {
        return tagRepository.findById(currentTagId)
                .map(tag -> {
                    tag.setNameTag(tagInput.getTagName());
                    return tagRepository.save(tag);
                });
    }

    public void deleteTag(Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);

        tag.ifPresent(value -> taggedTaskRepository.findByTag(value)
                .forEach(taggedTaskRepository::delete));

        tagRepository.deleteById(tagId);
    }

}
