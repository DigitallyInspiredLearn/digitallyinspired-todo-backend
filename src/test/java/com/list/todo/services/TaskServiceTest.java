package com.list.todo.services;

import com.list.todo.entity.Priority;
import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.entity.TodoListStatus;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    public void getAllTasksOnTodoList_OnExistentTodoList_ReturnsAListOfTasks() {
        // arrange
        Long todoListId = 2L;
        Long task1Id = 3L;
        Long task2Id = 4L;

        TodoList todoList = this.createTodoList();

        todoList.setId(todoListId);
        Task task1 = Task.builder()
                .body("ggggg")
                .isComplete(false)
                .todoList(todoList)
                .build();
        Task task2 = Task.builder()
                .body("zzzzz")
                .isComplete(false)
                .todoList(todoList)
                .build();

        task1.setId(task1Id);
        task2.setId(task2Id);

        todoList.getTasks().add(task1);
        todoList.getTasks().add(task2);

        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(todoList));
        when(taskRepositoryMock.findTasksByTodoListIdOrderByPriority(todoListId)).thenReturn(tasks);

        // act
        Iterable<Task> tasksFromService = taskService.getAllTasksOnTodoList(todoListId);

        // assert
        assertEquals(tasks, tasksFromService);

        verify(taskRepositoryMock).findTasksByTodoListIdOrderByPriority(2L);

    }

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

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.of(new TodoList()));
        when(taskRepositoryMock.save(any(Task.class))).thenReturn(new Task());

        TaskInput taskInput = new TaskInput("task 1", false, Priority.NOT_SPECIFIED, 1L);
        Task newTask = new Task();

        // act
        Optional<Task> addedTask = taskService.addTask(taskInput);

        // assert
        assertEquals(addedTask, Optional.of(newTask));
    }

    @Test
    public void addTask_OnNonExistentTodoList_ReturnsAnEmptyOptional() {
        // arrange
        Long todoListId = 100L;

        when(todoListService.getTodoListById(todoListId)).thenReturn(Optional.empty());

        TaskInput taskInput = new TaskInput("task 1", false, Priority.NOT_SPECIFIED, 1L);

        // act
        Optional<Task> addedTask = taskService.addTask(taskInput);

        // assert
        assertEquals(addedTask, Optional.empty());
    }

    @Test
    public void updateTask_OnNonExistentTodoList_ReturnsAnObjectOfUpdatedTask() {

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

        TaskInput taskInput = new TaskInput("task", false, Priority.NOT_SPECIFIED, todoListId);

        // act
        Optional<Task> taskFromService = taskService.updateTask(2L, taskInput);

        // assert
        assertEquals(taskFromService, Optional.of(updatedTask));

        verify(taskRepositoryMock).save(any(Task.class));
    }

    @Test
    public void updateTask_updateNonExistentTask_ReturnsAnEmptyOptional() {

        // arrange
        Long todoListId = 1L;
        Task task = Mockito.mock(Task.class);
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

        TaskInput taskInput = new TaskInput("task", false, Priority.NOT_SPECIFIED, todoListId);

        // act
        taskService.updateTask(2L, taskInput);

        // assert
        verify(task).setBody(taskInput.getBody());
        verify(task).setIsComplete(taskInput.getIsComplete());
        verify(task).setDurationTime(anyLong());
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

    private TodoList createTodoList() {
        return TodoList.builder()
                .todoListName("todoList")
                .todoListStatus(TodoListStatus.ACTIVE)
                .tasks(new LinkedHashSet<>())
                .build();
    }
}