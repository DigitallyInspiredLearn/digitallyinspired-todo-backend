package com.list.todo.init;

import com.list.todo.entity.Priority;
import com.list.todo.entity.Task;

import java.util.HashSet;
import java.util.Set;

public class TaskInit {

    public static Task createTask() {
        return Task.builder()
                .body("task")
                .isComplete(false)
                .priority(Priority.NOT_SPECIFIED)
                .build();
    }

    public static Set<Task> createSetOfTasks() {
        return new HashSet<Task>() {{
            add(createTask());
            add(createTask());
        }};
    }
}
