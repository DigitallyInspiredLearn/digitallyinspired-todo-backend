package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TagTaskKey;
import com.list.todo.payload.TagInput;
import com.list.todo.repositories.TagRepository;
import com.list.todo.repositories.TagTaskKeyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TagServiceTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long TAG_ID = 6L;
    private static final Long TASK_ID = 15L;
    private static final Long ANOTHER_USER_ID = 1000L;

    @Mock
    private TagRepository tagRepositoryMock;

    @Mock
    private TagTaskKeyRepository tagTaskKeyRepositoryMock;

    @Mock
    private TagTaskKeyService tagTaskKeyServiceMock;

    @InjectMocks
    private TagService tagServiceMock;

    @Test
    public void getTagById_OnExistentTag_ReturnsATag() {

        //arrange
        String nameTag = "Home";
        String color = "#1234";

        Tag tag = new Tag(nameTag, CURRENT_USER_ID, color);
        tag.setId(TAG_ID);

        when(tagRepositoryMock.findById(TAG_ID)).thenReturn(Optional.of(tag));

        //act
        Optional<Tag> tagFromService = tagServiceMock.getTagById(TAG_ID);

        //assert
        assertEquals(Optional.of(tag), tagFromService);


    }

    @Test
    public void getTagById_OnNonExistentTag_ReturnsEmptyOptional() {
        //arrange
        when(tagRepositoryMock.findById(TAG_ID)).thenReturn(Optional.empty());

        //act
        Optional<Tag> tagFromService = tagServiceMock.getTagById(TAG_ID);

        //assert
        assertEquals(Optional.empty(), tagFromService);

    }

    @Test
    public void getTagsByOwnerId_OnExistentOwnerId_ReturnsAListOfTags() {
        //arrange
        when(tagRepositoryMock.getByOwnerId(CURRENT_USER_ID)).thenReturn(createListOfTags(CURRENT_USER_ID));

        //act
        Iterable<Tag> tagsFromService = tagServiceMock.getTagsByOwnerId(CURRENT_USER_ID);

        //assert
        assertEquals(createListOfTags(CURRENT_USER_ID), tagsFromService);
    }

    @Test
    public void getTagsByOwnerId_OnNonExistentOwnerId_ReturnsNull() {
        //arrange
        when(tagRepositoryMock.getByOwnerId(ANOTHER_USER_ID)).thenReturn(new ArrayList<>());

        //act
        Iterable<Tag> tagsFromService = tagServiceMock.getTagsByOwnerId(ANOTHER_USER_ID);

        //assert
        assertEquals(new ArrayList<>(), tagsFromService);
    }

    @Test
    public void addTag_ReturnsAnObjectOfNewTag() {
        //arrange
        when(tagRepositoryMock.save(any(Tag.class))).thenReturn(new Tag());

        TagInput tagInput = new TagInput("Home", "jg");
        Tag newTag = new Tag();

        //act
        Optional<Tag> addedTag = tagServiceMock.addTag(tagInput, CURRENT_USER_ID);

        //assert
        assertEquals(addedTag, Optional.of(newTag));
    }

    @Test
    public void addTagToTask_ReturnsAnObjectOfNewTagTaskKey() {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "cc");

        when(tagTaskKeyServiceMock.addTagTaskKey(any(TagTaskKey.class))).thenReturn(Optional.of(new TagTaskKey()));

        Optional<TagTaskKey> newTagTaskKey = Optional.of(new TagTaskKey());

        //act
        Optional<TagTaskKey> addedTagTaskKey = tagServiceMock.addTagToTask(tag, TASK_ID);

        //assert
        assertEquals(addedTagTaskKey, newTagTaskKey);
    }

    @Test
    public void removeTagFromTask_SuccessfulDelete() {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "cc");

        //act
        tagServiceMock.removeTagFromTask(TASK_ID, tag);

        //assert
        verify(tagTaskKeyServiceMock).deleteTaggedTask(TASK_ID, tag);
    }

    @Test
    public void updateTag_updateNonExistentTag_ReturnsAnEmptyOptional() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        TagInput tagInput = new TagInput("Home", "ff");

        when(tagRepositoryMock.findById(TAG_ID)).thenReturn(Optional.empty());

        //act
        tagServiceMock.updateTag(2L, tagInput);

        //assert
        verify(tagRepositoryMock, never()).save(tag);
    }

    @Test
    public void updateTag_SuccessfulUpdate() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        TagInput tagInput = new TagInput("Home", "ff");

        when(tagRepositoryMock.findById(TAG_ID)).thenReturn(Optional.of(tag));

        //act
        tagServiceMock.updateTag(TAG_ID, tagInput);

        //assert
        verify(tag).setTagName(tagInput.getTagName());
        verify(tagRepositoryMock, times(1)).save(tag);

    }

    @Test
    public void deleteTag_deleteNonExistentTag_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);

        //act
        tagServiceMock.deleteTag(TAG_ID);

        //assert
        verify(tagTaskKeyRepositoryMock, never()).findByTag(tag);
        verify(tagRepositoryMock).deleteById(TAG_ID);
    }

    @Test
    public void deleteTag_deleteExistentTagAndTaggedTask_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);

        when(tagRepositoryMock.findById(TAG_ID)).thenReturn(Optional.of(tag));
        when(tagTaskKeyServiceMock.getTagTaskKeyByTag(tag)).thenReturn(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)));

        //act
        tagServiceMock.deleteTag(TAG_ID);

        //arrange
        verify(tagRepositoryMock).findById(TAG_ID);
        verify(tagTaskKeyServiceMock).getTagTaskKeyByTag(tag);
        createListOfTaggedTask(CURRENT_USER_ID)
                .forEach(taggedTask -> verify(tagTaskKeyServiceMock).deleteTaggedTask(taggedTask));
        verify(tagRepositoryMock).deleteById(TAG_ID);

    }
}
