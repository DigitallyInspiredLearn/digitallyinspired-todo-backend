package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Optional<Tag> getTagById(Long tagId) {
        return tagRepository.findById(tagId);
    }

    public Optional<Tag> addTag(String nameTag) {
        Tag newTag = Tag.builder()
                .nameTag(nameTag)
                .build();

        return Optional.of(tagRepository.save(newTag));
    }

    public Optional<Tag> updateTag(Long currentTagId, String nameTag) {
        return tagRepository.findById(currentTagId)
                .map(tag -> {
                    tag.setNameTag(nameTag);
                    return tagRepository.save(tag);
                });
    }

    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }

}
