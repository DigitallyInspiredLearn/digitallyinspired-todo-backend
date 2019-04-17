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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TagTaskKeyServiceTest {

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
        UserPrincipal currentUser = new UserPrincipal(1L, "V", "User", "c", "f", null);
        List<Long> tagsIds = new ArrayList<Long>() {{
            add(6L);
        }};

        Page<TodoList> todoListPage = new PageImpl<>(getTodoListByCreatedBy(), pageable, getTodoListByCreatedBy().size());

        doReturn(getTask()).when(tagTaskKeyServiceMock).getTasksByTags(tagsIds, currentUser.getId());
        when(todoListRepositoryMock.findDistinctByCreatedByAndTodoListStatusAndTasksIn(currentUser.getUsername(), TodoListStatus.ACTIVE,
                pageable, new ArrayList<>(getTask()))).thenReturn(todoListPage);
        when(tagTaskKeyRepositoryMock.findByTaskId(any())).thenReturn(getListOfTaggedTask());

        //act
        Set<TagTaskKey> taggedTasksFromService = tagTaskKeyServiceMock.getMyTaggedTask(currentUser, pageable, tagsIds);

        //assert
        assertEquals(new HashSet<>(getListOfTaggedTask()), taggedTasksFromService);
    }

    @Test
    public void getMyTaggedTask_OnNonExistentTagsByUser_ReturnsAListOfTaggedTask() {
        //arrange
        UserPrincipal currentUser = new UserPrincipal(1L, "V", "User", "c", "f", null);
        List<Long> tagsIds = new ArrayList<Long>() {{
        }};

        Page<TodoList> todoListPage = new PageImpl<>(getTodoListByCreatedBy(), pageable, getTodoListByCreatedBy().size());

        TagTaskKeyService tagTaskKeyService = mock(TagTaskKeyService.class);

        when(todoListRepositoryMock.findByCreatedBy(currentUser.getUsername(), pageable)).thenReturn(todoListPage);
        when(tagTaskKeyService.getTasksByTags(any(), any())).thenReturn(new HashSet<>());
        when(tagTaskKeyRepositoryMock.findByTaskId(any())).thenReturn(getListOfTaggedTask());

        //act
        Set<TagTaskKey> taggedTasksFromService = tagTaskKeyServiceMock.getMyTaggedTask(currentUser, pageable, tagsIds);

        //assert
        assertEquals(new HashSet<>(getListOfTaggedTask()), taggedTasksFromService);
    }

    @Test
    public void getTagTaskKeyByTag_OnExistentTag_ReturnsAObjectOfTagTaskKey() {
        //arrange
        Long tagId = 6L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId, "ff");
        tag.setId(tagId);

        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(getListOfTaggedTask());

        //act
        List<TagTaskKey> tagTaskKeysFromService = tagTaskKeyServiceMock.getTagTaskKeyByTag(tag);

        //assert
        assertEquals(getListOfTaggedTask(), tagTaskKeysFromService);
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
        when(tagTaskKeyRepositoryMock.findByTag(tag)).thenReturn(getListOfTaggedTask());
        when(taskRepositoryMock.getOne(3L)).thenReturn(task);

        //act
        Set<Task> tasksFromService = tagTaskKeyServiceMock.getTasksByTags(tagsIds, 1L);

        //assert
        assertEquals(getTask(), tasksFromService);

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

    private List<TagTaskKey> getListOfTaggedTask() {
        Long tagId = 6L;
        String nameTag = "Home";
        Long ownerId = 1L;

        Tag tag = new Tag(nameTag, ownerId, "ff");
        tag.setId(tagId);

        Long taskId = 3L;

        TagTaskKey tagTaskKey = new TagTaskKey(taskId, tag);
        tagTaskKey.setId(7L);

        return new ArrayList<TagTaskKey>() {{
            add(new TagTaskKey(taskId, tag));
        }};
    }

    private List<TodoList> getTodoListByCreatedBy() {
        String currentUser = "User";

        Task task1 = Task.builder()
                .body("gg")
                .isComplete(false)
                .createdBy(currentUser)
                .build();
        task1.setId(3L);

        Task task2 = Task.builder()
                .body("f")
                .isComplete(false)
                .createdBy(currentUser)
                .build();
        task2.setId(4L);

        Set<Task> tasks = new HashSet<Task>() {{
            add(task1);
            add(task2);
        }};

        TodoList todoList1 = TodoList.builder()
                .todoListName("q")
                .tasks(tasks)
                .createdBy(currentUser)
                .build();
        todoList1.setId(1L);

        TodoList todoList2 = TodoList.builder()
                .todoListName("fga")
                .createdBy(currentUser)
                .tasks(tasks)
                .build();
        todoList2.setId(5L);

        return new ArrayList<TodoList>() {{
            add(todoList1);
            add(todoList2);
        }};
    }

    private Set<Task> getTask() {
        String currentUser = "User";

        Task task1 = Task.builder()
                .body("gg")
                .isComplete(false)
                .createdBy(currentUser)
                .build();
        task1.setId(3L);

        return new HashSet<Task>() {{
            add(task1);
        }};
    }
}
