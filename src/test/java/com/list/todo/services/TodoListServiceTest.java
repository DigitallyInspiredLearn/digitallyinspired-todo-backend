//package com.list.todo.services;
//
//import com.list.todo.entity.Share;
//import com.list.todo.entity.TodoList;
//import com.list.todo.entity.User;
//import com.list.todo.payload.TodoListInput;
//import com.list.todo.repositories.TodoListRepository;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class TodoListServiceTest {
//
//    @Mock
//    private TodoListRepository todoListRepository;
//
//    @Mock
//    private ShareService shareService;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private TodoListService todoListService;
//
//    @Test
//    public void getTodoListById() {
//        // arrange
//        long todoListId = 1;
//        long userId = 1;
//        TodoList todoList = new TodoList();
//        todoList.setTodoListName("name");
//        todoList.setUserOwnerId(userId);
//        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(todoList));
//
//        // act
//        Optional<TodoList> returnedTodoList = todoListService.getTodoListById(todoListId);
//
//        // assert
//        Assert.assertEquals(returnedTodoList, Optional.of(todoList));
//    }
//
//    @Test
//    public void getTodoListsByUser() {
//        long userId = 1;
//        // TODO: Можно выделить в отдельный private метод создание листа
//        TodoList todoList1 = new TodoList();
//        todoList1.setTodoListName("name1");
//        todoList1.setUserOwnerId(userId);
//        TodoList todoList2 = new TodoList();
//        todoList2.setTodoListName("name2");
//        todoList2.setUserOwnerId(userId);
//        List<TodoList> todoLists = new ArrayList<>();
//        todoLists.add(todoList1);
//        todoLists.add(todoList2);
//        when(todoListRepository.findTodoListsByUserOwnerId(userId)).thenReturn(todoLists);
//
//        when(todoListRepository.findTodoListsByUserOwnerId(userId)).thenReturn(todoLists);
//
//        Iterable<TodoList> returnedTodoLists = todoListService.getTodoListsByUser(userId);
//
//        Assert.assertEquals(returnedTodoLists, todoLists);
//    }
//
//    @Test
//    public void addTodoList() {
//        long userId = 1;
//        TodoList todoList = new TodoList();
//        todoList.setTodoListName("name");
//        todoList.setUserOwnerId(userId);
//        when(todoListRepository.save(todoList)).thenReturn(todoList);
//
//        Optional<TodoList> addedTodoList = todoListService.addTodoList(
//                new TodoListInput(todoList.getTodoListName(), new LinkedHashSet<>()), userId);
//
//        verify(userService).getUserById(userId);
//        Assert.assertEquals(addedTodoList, Optional.of(todoList));
//        //verify(notificationService).notifyFollowersAboutAddingTodolist(any(User.class), any(TodoList.class));
//    }
//
//    @Test
//    public void updateTodoList() {
//        long todoListId = 1;
//        long userId = 1;
//        TodoList todoList = new TodoList();
//        todoList.setTodoListName("updatedName");
//        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(new TodoList()));
//        when(todoListRepository.save(todoList)).thenReturn(todoList);
//
//        Optional<TodoList> updatedTodoList = todoListService.updateTodoList(
//                todoListId, new TodoListInput(todoList.getTodoListName(), new LinkedHashSet<>()), userId);
//
//        Assert.assertEquals(updatedTodoList, Optional.of(todoList));
//    }
//
//    @Test
//    public void deleteTodoList() {
//        long todoListId = 1;
//        long userId = 1;
//        when(todoListRepository.findById(todoListId)).thenReturn(Optional.of(new TodoList()));
//
//        todoListService.deleteTodoList(todoListId, userId);
//        verify(shareService).deleteShareBySharedTodoListId(todoListId);
//    }
//
//    @Test
//    public void shareTodoList() {
//        String targetUserUsername = "vitaliy";
//        User targetUser = new User();
//        long ownerUserId = 1;
//        long sharedTodoListId = 1;
//        TodoList todoList = new TodoList();
//        todoList.setTodoListName("name");
//        todoList.setUserOwnerId(ownerUserId);
//        when(todoListRepository.findById(sharedTodoListId)).thenReturn(Optional.of(todoList));
//        when(userService.getUserByUsername(targetUserUsername)).thenReturn(Optional.of(targetUser));
//
//        todoListService.shareTodoList(targetUserUsername, sharedTodoListId, ownerUserId);
//        Share share = new Share(targetUser.getId(), todoList);
//
//        verify(todoListRepository).findById(sharedTodoListId);
//        verify(shareService).addShare(share);
//
//    }
//}