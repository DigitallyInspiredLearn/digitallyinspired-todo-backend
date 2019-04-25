package com.list.todo.services;

import com.list.todo.entity.*;
import com.list.todo.repositories.TagRepository;
import com.list.todo.repositories.TagTaskKeyRepository;
import com.list.todo.repositories.TaskRepository;
import com.list.todo.repositories.TodoListRepository;
import com.list.todo.security.UserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static com.list.todo.util.ObjectsProvider.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TagTaskKeyServiceTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final String CURRENT_USERNAME = "User";
    private static final String TODOLIST_NAME = "todoList";

    @Mock
    private TagTaskKeyRepository tagTaskKeyRepositoryMock;

    @Mock
    private TaskRepository taskRepositoryMock;

    @Mock
    private TodoListRepository todoListRepositoryMock;

    @Mock
    private TagRepository tagRepositoryMock;

    @Mock
    private Pageable pageable;

    private TagTaskKeyService tagTaskKeyServiceMock;

    @Before
    public void init() {
        tagTaskKeyServiceMock = Mockito.spy(new TagTaskKeyService(tagTaskKeyRepositoryMock,
                taskRepositoryMock, todoListRepositoryMock, tagRepositoryMock));
    }

    @Test
    public void addTagTaskKey_ReturnsAnObjectOfNewTagTaskKey() {
        //arrange
        when(tagTaskKeyRepositoryMock.save(any(TagTaskKey.class))).thenReturn(new TagTaskKey());

        Optional<TagTaskKey> newTagTaskKey = Optional.of(new TagTaskKey());

        //act
        Optional<TagTaskKey> addedTagTaskKey = tagTaskKeyServiceMock.addTagTaskKey(new TagTaskKey());

        //assert
        assertEquals(addedTagTaskKey, newTagTaskKey);
    }

    @Test
    public void getMyTaggedTask_OnExistentTagsByUser_ReturnsAListOfTaggedTask() {
        //arrange
        UserPrincipal currentUser = new UserPrincipal(CURRENT_USER_ID, "V", CURRENT_USERNAME, "c", "f", null);
        List<Long> tagsIds = new ArrayList<>() {{
            add(6L);
        }};

        Page<TodoList> todoListPage = new PageImpl<>(createListOfTodoLists(CURRENT_USERNAME), pageable, createListOfTodoLists(CURRENT_USERNAME).size());

        doReturn(createSetOfTasks(CURRENT_USERNAME)).when(tagTaskKeyServiceMock).getTasksByTags(tagsIds, currentUser.getId());
        when(todoListRepositoryMock.findDistinctByCreatedByAndTodoListStatusAndTasksInAndTodoListNameLike(currentUser.getUsername(), TodoListStatus.ACTIVE,
                pageable, new ArrayList<>(createSetOfTasks(CURRENT_USERNAME)), TODOLIST_NAME + "%")).thenReturn(todoListPage);
        when(tagTaskKeyRepositoryMock.findByTaskId(any())).thenReturn(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)));

        //act
        Set<TagTaskKey> taggedTasksFromService = tagTaskKeyServiceMock.getMyTaggedTask(currentUser, pageable, tagsIds, TODOLIST_NAME);

        //assert
        assertEquals(createListOfTaggedTask(CURRENT_USER_ID), taggedTasksFromService);
    }

    @Test
    public void getMyTaggedTask_OnNonExistentTagsByUser_ReturnsAListOfTaggedTask() {
        //arrange
        UserPrincipal currentUser = new UserPrincipal(1L, "V", "User", "c", "f", null);
        List<Long> tagsIds = new ArrayList<Long>() {{
        }};

        Page<TodoList> todoListPage = new PageImpl<>(createListOfTodoLists(CURRENT_USERNAME), pageable, createListOfTodoLists(CURRENT_USERNAME).size());

        TagTaskKeyService tagTaskKeyService = mock(TagTaskKeyService.class);

        when(todoListRepositoryMock.findByCreatedBy(currentUser.getUsername(), pageable)).thenReturn(todoListPage);
        when(tagTaskKeyService.getTasksByTags(any(), any())).thenReturn(new HashSet<>());
        when(tagTaskKeyRepositoryMock.findByTaskId(any())).thenReturn(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)));

        //act
        Set<TagTaskKey> taggedTasksFromService = tagTaskKeyServiceMock.getMyTaggedTask(currentUser, pageable, tagsIds, TODOLIST_NAME);

        //assert
        assertEquals(createListOfTaggedTask(CURRENT_USER_ID), taggedTasksFromService);
    }

    @Test
    public void getTagTaskKeyByTag_OnExistentTag_ReturnsAObjectOfTagTaskKey() {
        //arrange
        Long tagId = 6L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId, "ff");
        tag.setId(tagId);

        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)));

        //act
        List<TagTaskKey> tagTaskKeysFromService = tagTaskKeyServiceMock.getTagTaskKeyByTag(tag);

        //assert
        assertEquals(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)), tagTaskKeysFromService);
    }

    @Test
    public void getTagTaskKeyByTag_OnNonExistentTag_ReturnsNull() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);

        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(null);

        //act
        List<TagTaskKey> tagTaskKeysFromService = tagTaskKeyServiceMock.getTagTaskKeyByTag(tag);

        //assert
        assertNull(tagTaskKeysFromService);
    }

    @Test
    public void getTasksByTags_OnExistentTagsIdsList_ReturnsAListOfTask() {
        //arrange
        Task task = Task.builder()
                .body("gg")
                .isComplete(false)
                .createdBy("User")
                .build();
        task.setId(3L);

        List<Long> tagsIds = new ArrayList<Long>() {{
            add(6L);
        }};

        Tag tag = Tag.builder()
                .tagName("t")
                .ownerId(1L)
                .build();
        tag.setId(6L);

        when(tagRepositoryMock.findById(6L)).thenReturn(Optional.of(tag));
        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(new ArrayList<>(createListOfTaggedTask(CURRENT_USER_ID)));
        when(taskRepositoryMock.getOne(3L)).thenReturn(task);

        //act
        Set<Task> tasksFromService = tagTaskKeyServiceMock.getTasksByTags(tagsIds, 1L);

        //assert
        assertEquals(createSetOfTasks(CURRENT_USERNAME), tasksFromService);

    }

    @Test
    public void getTasksByTags_OnNonExistentTagsIdsList_ReturnsNull() {
        //arrange

        List<Long> tagsIds = new ArrayList<Long>() {{
        }};

        Tag tag = Mockito.mock(Tag.class);

        when(tagRepositoryMock.findById(any())).thenReturn(Optional.ofNullable(tag));
        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(new ArrayList<>());
        when(taskRepositoryMock.getOne(any())).thenReturn(null);

        //act
        Set<Task> tasksFromService = tagTaskKeyServiceMock.getTasksByTags(tagsIds, 1L);

        //assert
        assertEquals(new HashSet<>(), tasksFromService);

    }

    @Test
    public void deleteTaggedTask_DeleteNonExistentTaggedTask_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long taskId = 1L;
        TagTaskKey tagTaskKey = mock(TagTaskKey.class);

        //act
        tagTaskKeyServiceMock.deleteTaggedTask(taskId, tag);

        //assert
        verify(tagTaskKeyRepositoryMock, times(1)).findByTaskIdAndTag(taskId, tag);
        verify(tagTaskKeyRepositoryMock, times(0)).delete(tagTaskKey);
    }

    @Test
    public void deleteTaggedTask_DeleteExistentTaggedTask_Void() {
        //arrange
        Tag tag = Mockito.mock(Tag.class);
        Long taskId = 1L;
        TagTaskKey tagTaskKey = new TagTaskKey(taskId, tag);

        when(tagTaskKeyRepositoryMock.findByTaskIdAndTag(taskId, tag)).thenReturn(Optional.of(tagTaskKey));

        //act
        tagTaskKeyServiceMock.deleteTaggedTask(taskId, tag);

        //assert
        verify(tagTaskKeyRepositoryMock, times(1)).findByTaskIdAndTag(taskId, tag);
        verify(tagTaskKeyRepositoryMock, times(1)).delete(tagTaskKey);
    }

    @Test
    public void deleteTaggedTask_DeleteExistentTagTaskKey_Void() {
        //arrange
        TagTaskKey tagTaskKey = mock(TagTaskKey.class);

        //act
        tagTaskKeyServiceMock.deleteTaggedTask(tagTaskKey);

        //assert
        verify(tagTaskKeyRepositoryMock, times(1)).delete(tagTaskKey);
    }
}
