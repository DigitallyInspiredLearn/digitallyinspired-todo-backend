package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.entity.TodoList;
import com.list.todo.payload.TaskInput;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-todolists-before.sql", "/create-task-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-todolists-after.sql", "/create-user-after.sql", "/create-task-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void getAllTasksOnTodoList() {
        Iterable<Task> allTasks = taskService.getAllTasksOnTodoList(4L);
        ArrayList<Task> tasksFromDatabase = new ArrayList<>(2);

        for (Task task : allTasks) {
            tasksFromDatabase.add(task);
        }

        TodoList todoList = new TodoList("tl1", 1L, new LinkedHashSet<>());
        todoList.setId(4L);

        Task task1 = new Task("ggggg", false, todoList);
        Task task2 = new Task("zzzzz", false, todoList);
        task1.setId(8L);
        task2.setId(9L);
        todoList.getTasks().add(task1);
        todoList.getTasks().add(task2);
        ArrayList<Task> testTasks = new ArrayList<>();
        testTasks.add(task1);
        testTasks.add(task2);

        Assert.assertEquals(2, tasksFromDatabase.size());
        Assert.assertEquals(testTasks, tasksFromDatabase);
    }

    @Test
    public void getAllTasksOnTodoList_getTasksOnNonExistTodoList() {
        Iterable<Task> allTasks = taskService.getAllTasksOnTodoList(100L);

        Assert.assertNull(allTasks);
    }

    @Test
    public void addTask() {
        TaskInput newTask = new TaskInput("new task", false, 4L);
        Optional<Task> addedtask = taskService.addTask(newTask);

        Assert.assertEquals("new task", addedtask.get().getBody());
        Assert.assertFalse(addedtask.get().getIsComplete());
        Assert.assertEquals(Optional.of(4L), Optional.of(addedtask.get().getTodoList().getId()));

    }

    @Test
    public void addTask_FailTest() {
        TaskInput task = new TaskInput("new task", false, 100L);
        Optional<Task> newTask = taskService.addTask(task);

        Assert.assertEquals(Optional.empty(), newTask);

    }

    @Test
    public void updateTask() {
        TaskInput task = new TaskInput("updated task", true, 4L);
        Optional<Task> updatedTask = taskService.updateTask(8L, task);

        Assert.assertEquals("updated task", updatedTask.get().getBody());
        Assert.assertTrue(updatedTask.get().getIsComplete());
        Assert.assertEquals(Optional.of(4L), Optional.of(updatedTask.get().getTodoList().getId()));
    }

    @Test
    public void updateTask_FailTest() {
        TaskInput task = new TaskInput("updated task", true, 4L);
        Optional<Task> updatedTask = taskService.updateTask(8L, task);

        Assert.assertEquals("updated task", updatedTask.get().getBody());
        Assert.assertTrue(updatedTask.get().getIsComplete());
        Assert.assertEquals(Optional.of(4L), Optional.of(updatedTask.get().getTodoList().getId()));
    }

    @Test
    public void deleteTask() {
        taskService.deleteTask(9L);
    }
}