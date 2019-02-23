package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepositoryMock;

    @Mock
    private TodoListService todoListService;

    @InjectMocks
    private TaskService taskService;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllTasksOnTodoList() {

        Long userId = 1L;
        Long todoListId = 2L;
        Long task1Id = 3L;
        Long task2Id = 4L;

        TodoList todoList = new TodoList("tl1", userId, new LinkedHashSet<>());
        todoList.setId(todoListId);

        Task task1 = new Task("ggggg", false, todoList);
        Task task2 = new Task("zzzzz", false, todoList);
        task1.setId(task1Id);
        task2.setId(task2Id);

        todoList.getTasks().add(task1);
        todoList.getTasks().add(task2);

        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(taskRepositoryMock.findTasksByTodoListId(todoListId)).thenReturn(tasks);

        assertEquals(tasks, taskService.getAllTasksOnTodoList(todoListId));

        verify(taskRepositoryMock).findTasksByTodoListId(2L);

    }

    @Test
    public void getAllTasksOnTodoList_withNonExistentTodoList() {

        Long todoListId = 100L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        assertNull(taskService.getAllTasksOnTodoList(todoListId));
    }

    @Test
    public void addTask() {
        Long todoListId = 1L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(new TodoList()));
        when(taskRepositoryMock.save(any(Task.class))).thenReturn(new Task());

        TaskInput taskInput = new TaskInput("task 1", false, 1L);
        Task newTask = new Task();
        assertEquals(taskService.addTask(taskInput), Optional.of(newTask));
    }

    @Test
    public void addTask_withNonExistentTodoList() {
        Long todoListId = 100L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        TaskInput taskInput = new TaskInput("task 1", false, 1L);
        assertEquals(taskService.addTask(taskInput), Optional.empty());
    }

    @Test
    public void updateTask() {
        Long todoListId = 1L;

        TodoList todoList = TodoList.builder()
                .todoListName("todoList")
                .tasks(new LinkedHashSet<>())
                .build();
        todoList.setId(todoListId);

        Task oldTask = Task.builder()
                .body("task")
                .isComplete(false)
                .todoList(todoList)
                .build();

        Long taskId = 2L;
        oldTask.setId(taskId);
        todoList.getTasks().add(oldTask);

        Task updatedTask = Task.builder()
                .body("updatedTask")
                .isComplete(true)
                .todoList(todoList)
                .build();
        updatedTask.setId(taskId);

        when(taskRepositoryMock.findById(taskId)).thenReturn(Optional.of(oldTask));
        when(taskRepositoryMock.save(oldTask)).thenReturn(updatedTask);

        TaskInput taskInput = new TaskInput("task", false, todoListId);
        assertEquals(Optional.of(updatedTask), taskService.updateTask(2L, taskInput));

        verify(taskRepositoryMock).save(any(Task.class));
    }

    @Test
    public void updateTask_updateNonExistentTask() {
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

        when(taskRepositoryMock.findById(taskId)).thenReturn(Optional.empty());

        TaskInput taskInput = new TaskInput("task", false, todoListId);
        assertEquals(Optional.empty(), taskService.updateTask(taskId, taskInput));

        verify(taskRepositoryMock, times(0)).save(any(Task.class));
    }

    @Test
    public void deleteTask() {
        Long taskId = 1L;
        taskService.deleteTask(taskId);

        verify(taskRepositoryMock).deleteById(taskId);
    }
}