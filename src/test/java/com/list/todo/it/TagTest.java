package com.list.todo.it;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.controllers.TagController;
import com.list.todo.entity.Tag;
import com.list.todo.entity.TagTaskKey;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TagInput;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.TagService;
import com.list.todo.services.TagTaskKeyService;
import com.list.todo.services.TaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TagTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TagService tagServiceMock;

    @Mock
    private TagTaskKeyService tagTaskKeyServiceMock;

    @Mock
    private TaskService taskServiceMock;

    @InjectMocks
    private TagController tagControllerMock;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static Long CURRENT_USER_ID = 1L;
    private final static Long TAG_ID = 6L;
    private final static Long ANOTHER_USER_ID = 10L;
    private final static Long TASK_ID = 15L;

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(CURRENT_USER_ID);
            userPrincipal.setUsername("username");
            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("password"));

            return userPrincipal;
        }
    };

    private HandlerMethodArgumentResolver putSupportParameters = new HandlerMethodArgumentResolver() {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(Pageable.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return new PageRequest(0, 50);
        }
    };

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tagControllerMock)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putSupportParameters)
                .build();
    }

    @Test
    public void getMyTags_ReturnsAListOfTags() throws Exception {
        //arrange
        when(tagServiceMock.getTagsByOwnerId(CURRENT_USER_ID)).thenReturn(getListOfTags());

        //act
        MvcResult result = this.mockMvc.perform(get("/api/tags"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        //assert
        assertEquals(getTagsFromJsonResponse(result.getResponse().getContentAsString()), getListOfTags());

        verify(tagServiceMock, times(1)).getTagsByOwnerId(CURRENT_USER_ID);

    }

    @Test
    public void getMyTagsWithTodoListId_OnExistentTagsIds_ReturnsAListOfTagTaskKeys() throws Exception {
        //arrange
        List<Long> tagsIds = new ArrayList<Long>() {{
            add(TAG_ID);
        }};

        when(tagTaskKeyServiceMock.getMyTaggedTask(any(UserPrincipal.class), any(Pageable.class), eq(tagsIds)))
                .thenReturn(getListOfTaggedTask());

        //act
        MvcResult result = this.mockMvc.perform(get("/api/tags/myTagTaskKeys?tagId=6"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //assert
        assertEquals(new HashSet<>(getTagTaskKeyFromJsonResponse(result.getResponse().getContentAsString())), getListOfTaggedTask());

        verify(tagTaskKeyServiceMock, times(1)).getMyTaggedTask(any(UserPrincipal.class), any(Pageable.class), eq(tagsIds));
    }

    @Test
    public void addTag_ReturnsAObjectOfTag() throws Exception {
        //arrange
        TagInput tagInput = new TagInput("Home", "ff");


        when(tagServiceMock.addTag(tagInput, CURRENT_USER_ID)).thenReturn(Optional.of(new Tag("Home", CURRENT_USER_ID, "ff")));

        //act, assert
        this.mockMvc.perform(post("/api/tags")
                .content(objectMapper.writeValueAsString(tagInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("tagName").value(tagInput.getTagName()))
                .andExpect(jsonPath("ownerId").value(CURRENT_USER_ID))
                .andExpect(status().isOk());

        verify(tagServiceMock, times(1)).addTag(tagInput, CURRENT_USER_ID);
    }

    @Test
    public void updateTag_OnExistentTag_ReturnsAObjectOfTag() throws Exception {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "ff");
        tag.setId(TAG_ID);

        Tag updatedTag = new Tag("Job", CURRENT_USER_ID, "ff");
        updatedTag.setId(TAG_ID);

        TagInput tagInput = new TagInput("Job", "ff");

        when(tagServiceMock.getTagById(tag.getId())).thenReturn(Optional.of(tag));
        when(tagServiceMock.updateTag(tag.getId(), tagInput)).thenReturn(Optional.of(updatedTag));

        //act, assert
        this.mockMvc.perform(put("/api/tags/{id}", TAG_ID)
                .content(objectMapper.writeValueAsString(tagInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("id").value(updatedTag.getId()))
                .andExpect(jsonPath("tagName").value(tagInput.getTagName()))
                .andExpect(jsonPath("ownerId").value(CURRENT_USER_ID))
                .andExpect(status().isOk());

        verify(tagServiceMock, times(1)).getTagById(tag.getId());
        verify(tagServiceMock, times(1)).updateTag(tag.getId(), tagInput);
    }

    @Test
    public void updateTag_OnNonExistentTag_ReturnsANotFound() throws Exception {
        //arrange
        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.empty());

        TagInput tagInput = new TagInput("Job", "ff");

        //act, assert
        this.mockMvc.perform(put("/api/tags/{id}", TAG_ID)
                .content(objectMapper.writeValueAsString(tagInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).updateTag(TAG_ID, tagInput);
    }

    @Test
    public void updateTag_OnAnotherUser_ReturnsAIsForbidden() throws Exception {
        //arrange
        Tag tag = new Tag("Home", ANOTHER_USER_ID, "ff");

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));

        TagInput tagInput = new TagInput("Job", "ff");

        //act, assert
        this.mockMvc.perform(put("/api/tags/{id}", TAG_ID)
                .content(objectMapper.writeValueAsString(tagInput))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).updateTag(TAG_ID, tagInput);
    }

    @Test
    public void deleteTag_successfulDelete() throws Exception {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "ff");
        tag.setId(TAG_ID);

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));

        //act, assert
        this.mockMvc.perform(delete("/api/tags/{id}", TAG_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, times(1)).deleteTag(TAG_ID);
    }

    @Test
    public void deleteTag_OnNonExistentTag_ReturnsAIsNotFound() throws Exception {
        //arrange
        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.empty());

        //act, assert
        this.mockMvc.perform(delete("/api/tags/{id}", TAG_ID))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).deleteTag(TAG_ID);
    }

    @Test
    public void deleteTag_OnAnotherUser_ReturnsAIsForbidden() throws Exception {
        //arrange
        Tag tag = new Tag("Home", ANOTHER_USER_ID, "ff");

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));

        //act, assert
        this.mockMvc.perform(delete("/api/tags/{id}", TAG_ID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).deleteTag(TAG_ID);
    }

    @Test
    public void removeTagFromTheTask_OnExistentTag_SuccessfulDelete() throws Exception {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "ff");
        tag.setId(TAG_ID);

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));

        //act, assert
        this.mockMvc.perform(delete("/api/tags/removeTagFromTask/{id}?taskId=15", TAG_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, times(1)).removeTagFromTask(TASK_ID, tag);
    }

    @Test
    public void removeTagFromTheTask_OnNonExistentTag_ReturnsAIsNotFound() throws Exception {
        //arrange
        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.empty());

        //act, assert
        this.mockMvc.perform(delete("/api/tags/removeTagFromTask/{id}?taskId=15", TAG_ID))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).removeTagFromTask(TASK_ID, eq(any(Tag.class)));
    }

    @Test
    public void removeTagFromTheTask_OnAnotherUser_ReturnsAIsForbidden() throws Exception {
        //arrange
        Tag tag = new Tag("Home", ANOTHER_USER_ID, "ff");

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));

        //act, assert
        this.mockMvc.perform(delete("/api/tags/removeTagFromTask/{id}?taskId=15", TAG_ID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(tagServiceMock, never()).removeTagFromTask(TASK_ID, eq(any(Tag.class)));
    }

    @Test
    public void addTagToTask_OnExistentTagAndTask_ReturnsAObjectOfTagTaskKey() throws Exception {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "ff");
        tag.setId(TAG_ID);

        Task task = Task.builder()
                .body("task")
                .todoList(TodoList.builder()
                        .todoListName("ff")
                        .createdBy("username")
                        .build())
                .createdBy("username")
                .build();
        task.setId(TASK_ID);

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));
        when(taskServiceMock.getTaskById(TASK_ID)).thenReturn(Optional.of(task));
        when(tagServiceMock.addTagToTask(tag, TASK_ID)).thenReturn(Optional.of(new TagTaskKey(TASK_ID, tag)));

        //act, assert
        this.mockMvc.perform(post("/api/tags/{id}?taskId=15", tag.getId()))
                .andDo(print())
                .andExpect(jsonPath("$.tag.id").value(tag.getId()))
                .andExpect(jsonPath("taskId").value(task.getId()))
                .andExpect(status().isOk());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(taskServiceMock, times(1)).getTaskById(TASK_ID);
        verify(tagServiceMock, times(1)).addTagToTask(tag, TASK_ID);
    }

    @Test
    public void addTagToTask_OnNonExistentTagAndTask_ReturnsAIsNotFound() throws Exception {
        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.empty());
        when(taskServiceMock.getTaskById(TASK_ID)).thenReturn(Optional.empty());

        //act, assert
        this.mockMvc.perform(post("/api/tags/{id}?taskId=2", TAG_ID))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(taskServiceMock, never()).getTaskById(TASK_ID);
        verify(tagServiceMock, never()).addTagToTask(eq(any(Tag.class)), TASK_ID);
    }

    @Test
    public void addTagToTask_OnNonExistentTodoList_ReturnsAIsNotFound() throws Exception {
        //arrange
        Tag tag = new Tag("Home", CURRENT_USER_ID, "ff");
        tag.setId(TAG_ID);

        Task task = Task.builder()
                .body("task")
                .todoList(null)
                .createdBy("username")
                .build();
        task.setId(TASK_ID);

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));
        when(taskServiceMock.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

        //act, assert
        this.mockMvc.perform(post("/api/tags/{id}?taskId=15", TAG_ID))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(taskServiceMock, times(1)).getTaskById(TASK_ID);
        verify(tagServiceMock, never()).addTagToTask(tag, TASK_ID);
    }

    @Test
    public void addTagToTask_OnAnotherUser_ReturnsAIsForbidden() throws Exception {
        //arrange
        Tag tag = new Tag("Home", ANOTHER_USER_ID, "ff");
        tag.setId(TAG_ID);

        Task task = Task.builder()
                .body("task")
                .todoList(TodoList.builder()
                        .todoListName("ff")
                        .createdBy("username2")
                        .build())
                .createdBy("username2")
                .build();
        task.setId(TASK_ID);

        when(tagServiceMock.getTagById(TAG_ID)).thenReturn(Optional.of(tag));
        when(taskServiceMock.getTaskById(TASK_ID)).thenReturn(Optional.of(task));

        //act, assert
        this.mockMvc.perform(post("/api/tags/{id}?taskId=15", TAG_ID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(tagServiceMock, times(1)).getTagById(TAG_ID);
        verify(taskServiceMock, times(1)).getTaskById(TASK_ID);
        verify(tagServiceMock, never()).addTagToTask(tag, TASK_ID);

    }

    private List<Tag> getTagsFromJsonResponse(String response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Tag.class);

        return objectMapper.readValue(response, type);
    }

    private List<TagTaskKey> getTagTaskKeyFromJsonResponse(String response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, TagTaskKey.class);

        return objectMapper.readValue(response, type);
    }

    private List<Tag> getListOfTags() {
        Long tagId = 2L;
        String nameTag = "Home";

        Tag tag = new Tag(nameTag, CURRENT_USER_ID, "fg");
        tag.setId(tagId);

        Long tag2Id = 3L;
        String name2Tag = "Home";

        Tag tag2 = new Tag(name2Tag, CURRENT_USER_ID, "ff");
        tag2.setId(tag2Id);

        return new ArrayList<Tag>() {{
            add(tag);
            add(tag2);
        }};
    }

    private Set<TagTaskKey> getListOfTaggedTask() {
        Long tagId = 6L;
        String nameTag = "";

        Tag tag = new Tag(nameTag, CURRENT_USER_ID, "");
        tag.setId(tagId);

        Long taskId = 3L;

        TagTaskKey tagTaskKey = new TagTaskKey(taskId, tag);
        tagTaskKey.setId(7L);

        return new HashSet<TagTaskKey>() {{
            add(tagTaskKey);
        }};
    }


}
