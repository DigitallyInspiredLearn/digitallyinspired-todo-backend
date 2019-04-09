package com.list.todo.services;

import com.list.todo.entity.Tag;
import com.list.todo.entity.TagTaskKey;
import com.list.todo.entity.Task;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TagTaskKeyServiceTest {

    @Mock
    private TagTaskKeyRepository tagTaskKeyRepositoryMock;

    @Mock
    private TaskService taskServiceMock;

    @Mock
    private TagService tagServiceMock;

    @InjectMocks
    private TagTaskKeyService tagTaskKeyServiceMock;

    @Test
    public void addTagToTask_OnExistentTaskAndTag_ReturnsAnObjectOfNewTaggedTask() {
        //arrange
        Long taskId = 1L;
        Long tagId = 2L;

        Tag tag = Mockito.mock(Tag.class);
        Task task = Mockito.mock(Task.class);
        TagTaskKey tagTaskKey = new TagTaskKey();

        when(taskServiceMock.getTaskById(taskId)).thenReturn(Optional.of(task));
        when(tagServiceMock.getTagById(tagId)).thenReturn(Optional.of(tag));
        when(tagTaskKeyRepositoryMock.save(any(TagTaskKey.class))).thenReturn(new TagTaskKey());

        //act
        Optional<TagTaskKey> addedTaggedTask = tagTaskKeyServiceMock.addTaggedTask(tagTaskKey);

        //assert
        verify(tagTaskKeyRepositoryMock).save(new TagTaskKey(task.getId(), tag));
        assertEquals(addedTaggedTask, Optional.of(tagTaskKey));

    }

    @Test
    public void addTagToTask_OnNonExistentTaskAndTag_ReturnsAnEmptyOptional() {
        //arrange
        Long taskId = 1L;
        Long tagId = 2L;

        Tag tag = Mockito.mock(Tag.class);
        Task task = Mockito.mock(Task.class);

        when(taskServiceMock.getTaskById(taskId)).thenReturn(Optional.empty());
        when(tagServiceMock.getTagById(tagId)).thenReturn(Optional.empty());

        //act
        Optional<TagTaskKey> addedTaggedTask = tagTaskKeyServiceMock.addTaggedTask(null);

        //assert
        verify(tagTaskKeyRepositoryMock, times(0)).save(new TagTaskKey(task.getId(), tag));
        assertEquals(addedTaggedTask, Optional.empty());
    }

//    @Test
//    public void getMyTaggedTask_OnExistentTagsByUser_ReturnsAListOfTaggedTask() {
//        //arrange
//        Long currentUserId = 1L;
//        Tag tag = Mockito.mock(Tag.class);
//
//        when(tagServiceMock.getTagsByOwnerId(currentUserId)).thenReturn(getListOfTags());
//        when(tagTaskKeyServiceMock.getTaggedTaskByTag(tag)).thenReturn(getListOfTaggedTask());
//
//        //act
//        Iterable<TagTaskKey> taggedTasksFromService = tagTaskKeyServiceMock.getMyTaggedTask(currentUserId);
//
//        //assert
//        assertEquals(getListOfTaggedTask(), taggedTasksFromService);
//    }

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

    private List<TagTaskKey> getListOfTaggedTask() {
        Long tagId = 1L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId);
        tag.setId(tagId);

        Long taskId = 3L;
        Long task2Id = 4L;

        return new ArrayList<TagTaskKey>() {{
            new TagTaskKey(taskId, tag);
            new TagTaskKey(task2Id, tag);
        }};
    }
}
