package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepositoryMock;

    @Mock
    private TodoListService todoListService;

    @InjectMocks
    private TaskService taskService;


    @Test
    public void getAllTasksOnTodoList_OnNonExistentTodoList_ReturnsNull() {
        // arrange
        Long todoListId = 100L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        Iterable<Task> tasksFromService = taskService.getAllTasksOnTodoList(todoListId);

        // assert
        assertNull(tasksFromService);
    }

    @Test
    public void addTask_OnExistentTodoList_ReturnsAnObjectOfNewTask() {
        // arrange
        Long todoListId = 1L;
        TodoList todoList = new TodoList();
        todoList.setId(todoListId);
        TaskInput taskInput = new TaskInput("task 1", false, 1L);
        Task newTask = Task.builder()
                .body(taskInput.getBody())
                .isComplete(taskInput.getIsComplete())
                .todoList(todoList)
                .build();

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(taskRepositoryMock.save(any(Task.class))).thenReturn(newTask);

        // act
        Optional<Task> addedTask = taskService.addTask(taskInput);

        // assert
        verify(taskRepositoryMock).save(newTask);
        assertEquals(addedTask, Optional.of(newTask));
    }

    @Test
    public void addTask_OnNonExistentTodoList_ReturnsAnEmptyOptional() {
        // arrange
        Long todoListId = 100L;
        TaskInput taskInput = new TaskInput("task 1", false, 1L);

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        // act
        Optional<Task> addedTask = taskService.addTask(taskInput);

        // assert
        assertEquals(addedTask, Optional.empty());
    }


    @Test
    public void updateTask_updateNonExistentTask_ReturnsAnEmptyOptional() {
        // arrange
        Long todoListId = 1L;

        TodoList todoList = TodoList.builder()
                .todoListName("todoList")
                .tasks(new LinkedHashSet<>())
                .build();
        todoList.setId(todoListId);

        Task oldTask = Task.builder()
                .body("task")
                .isComplete(false)
                .build();
        Long taskId = 1000L;
        oldTask.setId(taskId);
        todoList.getTasks().add(oldTask);
        TaskInput taskInput = new TaskInput("task", false, todoListId);

        when(taskRepositoryMock.findById(taskId)).thenReturn(Optional.empty());

        // act
        Optional<Task> taskFromService = taskService.updateTask(2L, taskInput);

        // assert
        assertEquals(taskFromService, Optional.empty());

        verify(taskRepositoryMock, times(0)).save(any(Task.class));
    }

    @Test
    public void updateTask_SuccessfulUpdate() {
        // arrange
        Task task = Mockito.mock(Task.class);
        Long todoListId = 1L;
        Long taskId = 1000L;
        TaskInput taskInput = new TaskInput("updatedTask", true, todoListId);

        when(taskRepositoryMock.findById(taskId)).thenReturn(Optional.of(task));

        // act
        taskService.updateTask(taskId, taskInput);

        // assert
        verify(task).setBody(taskInput.getBody());
        verify(task).setIsComplete(taskInput.getIsComplete());
        verify(task).setRealizationTime(anyLong());
        verify(task).setCompletedDate(anyLong());
        verify(taskRepositoryMock, times(1)).save(task);
    }

    @Test
    public void deleteTask_OnNonExistentTodoList_Void() {

        // arrange
        Long taskId = 1L;

        // act
        taskService.deleteTask(taskId);

        // assert
        verify(taskRepositoryMock).deleteById(taskId);
    }
}