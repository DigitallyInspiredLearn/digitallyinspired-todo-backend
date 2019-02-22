package com.list.todo.services;

import com.list.todo.entity.Task;
import com.list.todo.payload.TaskInput;
import com.list.todo.repositories.TaskRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-todolists-before.sql", "/create-task-before.sql", "/update-sequence"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-todolists-after.sql", "/create-user-after.sql", "/create-task-after.sql", "/create-shares-after.sql", "/create-followers-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void getAllTasksOnTodoList() {
        // Был изменен TaskService(удалена проверка currentUser==userOwner для todoList)
        long todoListId = 4;
        Iterable<Task> tasks = taskService.getAllTasksOnTodoList(todoListId);
        Iterable<Task> tasksFromRepo = taskRepository.findTasksByTodoListId(todoListId);
        Assert.assertEquals(tasks, tasksFromRepo);

    }

    @Test
    public void addTask() {
        TaskInput taskInput = new TaskInput("body", false, 4L);
        Optional<Task> task = taskService.addTask(taskInput);
        Assert.assertTrue(task.isPresent());
        Optional<Task> taskFromRepo = taskRepository.findById(task.get().getId());
        Assert.assertEquals(task, taskFromRepo);
    }

    @Test
    public void updateTask() {
        long currentTaskId = 9;
        TaskInput taskInput = new TaskInput();
        taskInput.setBody("body");
        taskInput.setIsComplete(false);
        Optional<Task> updatedTask = taskService.updateTask(currentTaskId, taskInput);
        Assert.assertTrue(updatedTask.isPresent());
        Optional<Task> updatedTaskFromRepo = taskRepository.findById(currentTaskId);
        Assert.assertEquals(updatedTask, updatedTaskFromRepo);
    }

    @Test
    public void deleteTask() {
        long taskId = 8;
        taskService.deleteTask(taskId);
        Optional<Task> taskFromRepo = taskRepository.findById(taskId);
        Assert.assertFalse(taskFromRepo.isPresent());
    }
}