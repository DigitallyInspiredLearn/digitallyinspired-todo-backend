package com.list.todo.services;

import com.list.todo.entity.Share;
import com.list.todo.entity.TodoList;
import com.list.todo.repositories.ShareRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static com.list.todo.util.ObjectsProvider.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShareServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long TODO_LIST_ID = 2L;

    @Mock
    private ShareRepository shareRepository;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private ShareService shareService;

    @Test
    public void getSharedTodoListsByUser_WithoutPagination_IterableOfTodoLists() {
        // arrange
        List<Share> shareList = createListOfShares();
        List<TodoList> sharedTodoLists = new ArrayList<TodoList>() {{
            add(shareList.get(0).getSharedTodoList());
        }};

        when(shareRepository.findBySharedUserId(USER_ID)).thenReturn(shareList);

        // act
        Iterable<TodoList> result = shareService.getSharedTodoListsByUser(USER_ID);

        // assert
        Assert.assertEquals(sharedTodoLists, result);
        verify(shareRepository, times(1)).findBySharedUserId(USER_ID);
    }

    @Test
    public void getSharedTodoListsByUser_WithPagination_IterableOfTodoLists() {
        // arrange
        List<Share> shareList = createListOfShares();
        List<TodoList> sharedTodoLists = new ArrayList<TodoList>() {{
            add(shareList.get(0).getSharedTodoList());
        }};
        Page<Share> sharesPage = new PageImpl<>(shareList, pageable, shareList.size());
        Page<TodoList> sharedTodoListsPage = new PageImpl<>(sharedTodoLists, pageable, sharedTodoLists.size());

        when(shareRepository.findBySharedUserId(USER_ID, pageable)).thenReturn(sharesPage);

        // act
        Iterable<TodoList> result = shareService.getSharedTodoListsByUser(USER_ID, pageable);

        // assert
        Assert.assertEquals(sharedTodoListsPage, result);
        verify(shareRepository, times(1)).findBySharedUserId(USER_ID, pageable);
    }

    @Test
    public void addShare_AddNewShare() {
        // arrange
        Share share = new Share();

        // act
        shareService.addShare(share);

        // assert
        verify(shareRepository, times(1)).save(share);
    }

    @Test
    public void isSharedTodoListToUser() {
        // arrange
        boolean isShared = true;
        TodoList todoList = createTodoList();
        List<Share> shareList = createListOfShares();

        when(shareRepository.findBySharedUserId(USER_ID)).thenReturn(shareList);

        // act
        boolean result = shareService.isSharedTodoListToUser(todoList, USER_ID);

        // assert
        Assert.assertEquals(isShared, result);
    }

    @Test
    public void isSharedTodoList_VerifyIsSharedTodoList() {
        // arrange
        boolean isExist = false;
        when(shareRepository.existsBySharedTodoListId(TODO_LIST_ID)).thenReturn(isExist);

        // act
        boolean result = shareService.isSharedTodoList(TODO_LIST_ID);

        // assert
        Assert.assertEquals(isExist, result);
        verify(shareRepository).existsBySharedTodoListId(TODO_LIST_ID);
    }

    @Test
    public void deleteShareBySharedTodoListId() {
        // act
        shareService.deleteShareBySharedTodoListId(TODO_LIST_ID);

        // assert
        verify(shareRepository).deleteBySharedTodoListId(TODO_LIST_ID);
    }
}