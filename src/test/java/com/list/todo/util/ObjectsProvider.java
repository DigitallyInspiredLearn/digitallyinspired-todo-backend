package com.list.todo.util;

import com.list.todo.entity.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectsProvider {

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
        }};
    }

    public static List<Task> createListOfTasks() {
        return new ArrayList<Task>() {{
            add(createTask());
        }};
    }

    public static TodoList createTodoList() {
        return TodoList.builder()
                .todoListName("todoListName")
                .createdBy("username")
                .comment("comment")
                .todoListStatus(TodoListStatus.ACTIVE)
                .tasks(createSetOfTasks())
                .build();
    }

    public static List<TodoList> createListOfTodoLists() {
        TodoList todoList1 = createTodoList();
        todoList1.setId(1L);

        TodoList todoList2 = createTodoList();
        todoList2.setId(2L);

        return new ArrayList<TodoList>() {{
            add(todoList1);
            add(todoList2);
        }};
    }

    public static Tag createTag() {
        return Tag.builder()
                .tagName("tag")
                .color("color")
                .ownerId(3L)
                .build();
    }

    public static List<Tag> createSetOfTags() {
        return new ArrayList<Tag>() {{
            add(createTag());
            add(createTag());
        }};
    }
}
