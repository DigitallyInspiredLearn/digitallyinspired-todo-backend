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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.list.todo.util.ObjectsProvider.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskServiceTest {

    private static final Long TODO_LIST_ID = 1L;
    private static final Long TASK_ID_1 = 10L;
    private static final Long TASK_ID_2 = 11L;
    private static final String CREATED_BY = "username";
    private static final Long NUMBER_OF_TASKS = 5L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TodoListService todoListService;

    @Mock
    private TagTaskKeyService tagTaskKeyService;

    @InjectMocks
    private TaskService taskService;


    @Test
    public void getTaskById_OnExistentTask_ReturnsAnObjectOfTask() {
        //arrange
        Task task = createTask();
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.ofNullable(task));

        //act
        Optional<Task> returnedTask = taskService.getTaskById(TASK_ID_1);

        //assert
        assertEquals(task, returnedTask.get());
    }

    @Test
    public void countTasksByCreatedBy_ReturnsLong() {
        //arrange
        when(taskRepository.countByCreatedBy(CREATED_BY)).thenReturn(NUMBER_OF_TASKS);

        //act
        Long returnedCount = taskService.countTasksByCreatedBy(CREATED_BY);

        //assert
        assertEquals(NUMBER_OF_TASKS, returnedCount);
        verify(taskRepository).countByCreatedBy(CREATED_BY);
    }

    @Test
    public void countTasksByCreatedByAndIsComplete_ReturnsLong() {
        //arrange
        when(taskRepository.countByCreatedByAndIsComplete(CREATED_BY, true)).thenReturn(NUMBER_OF_TASKS);

        //act
        Long returnedCount = taskService.countTasksByCreatedByAndIsComplete(CREATED_BY, true);

        //assert
        assertEquals(NUMBER_OF_TASKS, returnedCount);
        verify(taskRepository).countByCreatedByAndIsComplete(CREATED_BY, true);
    }

    @Test
    public void getAllTasksOnTodoList_OnExistentTodoList_ReturnsAListOfTasks() {
        // arrange
        TodoList todoList = createTodoList();
        todoList.setId(TODO_LIST_ID);
        Task task1 = createTask();
        Task task2 = createTask();
        task1.setId(TASK_ID_1);
        task2.setId(TASK_ID_2);
        todoList.setTasks(Set.of(task1, task2));
        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.of(todoList));
        when(taskRepository.findTasksByTodoListIdOrderByPriority(TODO_LIST_ID)).thenReturn(tasks);

        // act
        Iterable<Task> tasksFromService = taskService.getAllTasksOnTodoList(TODO_LIST_ID);

        // assert
        assertEquals(tasks, tasksFromService);
        verify(todoListService).getTodoListById(TODO_LIST_ID);
        verify(taskRepository).findTasksByTodoListIdOrderByPriority(TODO_LIST_ID);
    }

    @Test
    public void getAllTasksOnTodoList_OnNonExistentTodoList_ReturnsNull() {
        // arrange
        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        // act
        Iterable<Task> tasksFromService = taskService.getAllTasksOnTodoList(TODO_LIST_ID);

        // assert
        assertNull(tasksFromService);
        verify(todoListService).getTodoListById(TODO_LIST_ID);
    }

    @Test
    public void addTask_OnExistentTodoList_ReturnsAnOptionalOfNewTask() {
        // arrange
        TaskInput taskInput = createTaskInput(TODO_LIST_ID);
        Optional<TodoList> todoList = Optional.of(createTodoList());
        Task task = Task.builder()
                .body(taskInput.getBody())
                .isComplete(taskInput.getIsComplete())
                .priority(taskInput.getPriority())
                .todoList(todoList.get())
                .build();

        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(todoList);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // act
        Optional<Task> addedTask = taskService.addTask(taskInput);

        // assert
        assertEquals(task, addedTask.get());
        verify(todoListService).getTodoListById(TODO_LIST_ID);
        verify(taskRepository).save(task);
    }

    @Test
    public void addTask_OnNonExistentTodoList_ReturnsAnEmptyOptional() {
        // arrange
        when(todoListService.getTodoListById(TODO_LIST_ID)).thenReturn(Optional.empty());

        // act
        Optional<Task> addedTask = taskService.addTask(createTaskInput(TODO_LIST_ID));

        // assert
        assertEquals(addedTask, Optional.empty());
        verify(todoListService).getTodoListById(TODO_LIST_ID);
    }

    @Test
    public void updateTask_OnExistentTask_ReturnsAnOptionalOfUpdatedTask() {
        // arrange
        TaskInput taskInput = createTaskInput(TODO_LIST_ID);
        Task oldTask = mock(Task.class);
        oldTask.setId(TASK_ID_1);
        Task updatedTask = createTask();
        updatedTask.setId(TASK_ID_1);

        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(oldTask));
        when(taskRepository.save(oldTask)).thenReturn(updatedTask);

        // act
        Optional<Task> taskFromService = taskService.updateTask(TASK_ID_1, taskInput);

        // assert
        assertEquals(updatedTask, taskFromService.get());
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskRepository).save(oldTask);
        verify(oldTask).setBody(taskInput.getBody());
        verify(oldTask).setIsComplete(taskInput.getIsComplete());
        verify(oldTask).setPriority(taskInput.getPriority());
        verify(oldTask).setDurationTime(taskInput.getDurationTime());
        verify(oldTask, times(0)).setCompletedDate(anyLong());
    }

    @Test
    public void updateTask_SetIsCompleteToTrue_ReturnsAnOptionalOfUpdatedTask() {
        // arrange
        TaskInput taskInput = createTaskInput(TODO_LIST_ID);
        taskInput.setIsComplete(true);
        Task oldTask = mock(Task.class);
        Task updatedTask = createTask();
        updatedTask.setId(TASK_ID_1);

        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(oldTask));
        when(taskRepository.save(oldTask)).thenReturn(updatedTask);

        // act
        Optional<Task> taskFromService = taskService.updateTask(TASK_ID_1, taskInput);

        // assert
        assertEquals(updatedTask, taskFromService.get());
        verify(taskRepository).findById(TASK_ID_1);
        verify(oldTask).setBody(taskInput.getBody());
        verify(oldTask).setIsComplete(taskInput.getIsComplete());
        verify(oldTask).setPriority(taskInput.getPriority());
        verify(oldTask).setDurationTime(taskInput.getDurationTime());
        verify(oldTask).setCompletedDate(anyLong());
        verify(taskRepository).save(oldTask);
    }

    @Test
    public void deleteTask_OnExistentTask_SuccessfulDeleting() {
        // act
        taskService.deleteTask(TASK_ID_1);

        // assert
        verify(tagTaskKeyService).deleteTaggedTask(TASK_ID_1);

        verify(taskRepository).deleteById(TASK_ID_1);
    }
}