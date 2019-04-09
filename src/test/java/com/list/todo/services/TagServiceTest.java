package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TaggedTask;
import com.list.todo.payload.TagInput;
import com.list.todo.repositories.TagRepository;
import com.list.todo.repositories.TaggedTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepositoryMock;

    @Mock
    private TaggedTaskRepository taggedTaskRepositoryMock;

    @InjectMocks
    private TagService tagServiceMock;

    @Test
    public void getTagById_OnExistentTag_ReturnsATag() {

        //arrange
        Long tagId = 1L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId);
        tag.setId(tagId);

        when(tagRepositoryMock.findById(tagId)).thenReturn(Optional.of(tag));

        //act
        Optional<Tag> tagFromService = tagServiceMock.getTagById(tagId);

        //assert
        assertEquals(Optional.of(tag), tagFromService);


    }

    @Test
    public void getTagById_OnNonExistentTag_ReturnsEmptyOptional() {
        //arrange
        Long tagId = 1000L;

        when(tagRepositoryMock.findById(tagId)).thenReturn(Optional.empty());

        //act
        Optional<Tag> tagFromService = tagServiceMock.getTagById(tagId);

        //assert
        assertEquals(Optional.empty(), tagFromService);

    }

    @Test
    public void getTagsByOwnerId_OnExistentOwnerId_ReturnsAListOfTags() {
        //arrange
        Long ownerId = 1L;

        when(tagRepositoryMock.getByOwnerId(ownerId)).thenReturn(getListOfTags());

        //act
        Iterable<Tag> tagsFromService = tagServiceMock.getTagsByOwnerId(ownerId);

        //assert
        assertEquals(getListOfTags(), tagsFromService);
    }

    @Test
    public void getTagsByOwnerId_OnNonExistentOwnerId_ReturnsNull() {
        //arrange
        Long ownerId = 1000L;

        when(tagRepositoryMock.getByOwnerId(ownerId)).thenReturn(new ArrayList<>());

        //act
        Iterable<Tag> tagsFromService = tagServiceMock.getTagsByOwnerId(ownerId);

        //assert
        assertEquals(new ArrayList<>(), tagsFromService);
    }

    @Test
    public void addTag_ReturnsAnObjectOfNewTag() {
        //arrange
        Long currentUserId = 1L;

        when(tagRepositoryMock.save(any(Tag.class))).thenReturn(new Tag());

        TagInput tagInput = new TagInput("Home");
        Tag newTag = new Tag();

        //act
        Optional<Tag> addedTag = tagServiceMock.addTag(tagInput, currentUserId);

        //assert
        assertEquals(addedTag, Optional.of(newTag));
    }

    @Test
    public void updateTag_updateNonExistentTag_ReturnsAnEmptyOptional() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long tagId = 1000L;
        TagInput tagInput = new TagInput("Home");

        when(tagRepositoryMock.findById(tagId)).thenReturn(Optional.empty());

        //act
        tagServiceMock.updateTag(2L, tagInput);

        //assert
        verify(tagRepositoryMock, times(0)).save(tag);
    }

    @Test
    public void updateTag_SuccessfulUpdate() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long tagId = 1000L;
        TagInput tagInput = new TagInput("Home");

        when(tagRepositoryMock.findById(tagId)).thenReturn(Optional.of(tag));

        //act
        tagServiceMock.updateTag(tagId, tagInput);

        //assert
        verify(tag).setTagName(tagInput.getTagName());
        verify(tagRepositoryMock, times(1)).save(tag);

    }

    @Test
    public void deleteTag_deleteNonExistentTag_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long tagId = 1L;

        //act
        tagServiceMock.deleteTag(tagId);

        //assert
        verify(taggedTaskRepositoryMock, times(0)).findByTag(tag);
        verify(tagRepositoryMock).deleteById(tagId);
    }

    @Test
    public void deleteTag_deleteExistentTagAndTaggedTask_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long tagId = 1L;

        when(tagRepositoryMock.findById(tagId)).thenReturn(Optional.of(tag));
        when(taggedTaskRepositoryMock.findByTag(tag)).thenReturn(getListOfTaggedTask());

        //act
        tagServiceMock.deleteTag(tagId);

        //arrange
        verify(tagRepositoryMock).findById(tagId);
        verify(taggedTaskRepositoryMock).findByTag(tag);
        getListOfTaggedTask()
                .forEach(taggedTask -> verify(taggedTaskRepositoryMock).delete(taggedTask));
        verify(tagRepositoryMock).deleteById(tagId);

    }

    private List<Tag> getListOfTags() {
        Long tagId = 1L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId);
        tag.setId(tagId);

        Long tag2Id = 2L;
        String name2Tag = "Home";

        Tag tag2 = new Tag(name2Tag, ownerId);
        tag2.setId(tag2Id);

        return new ArrayList<Tag>() {{
            add(tag);
            add(tag2);
        }};
    }

    private List<TaggedTask> getListOfTaggedTask() {
        Long tagId = 1L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId);
        tag.setId(tagId);

        Long taskId = 3L;
        Long task2Id = 4L;

        return new ArrayList<TaggedTask>() {{
            new TaggedTask(taskId, tag);
            new TaggedTask(task2Id, tag);
        }};
    }
}
