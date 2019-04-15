package com.list.todo.init;

import com.list.todo.entity.TodoList;

import java.util.ArrayList;
import java.util.List;

public class TodoListInit {
    public static TodoList createTodoList() {
        return TodoList.builder()
                .todoListName("todoListName")
                .createdBy("username")
                .tasks(TaskInit.createSetOfTasks())
                .build();
    }

    public static List<TodoList> createListOfTodoLists() {
        int countOfTodoLists = 2;
        List<TodoList> todoLists = new ArrayList<>(countOfTodoLists);

        for (long i = 0; i < countOfTodoLists; i++) {
            TodoList todoList = createTodoList();
            todoList.setId(i);

            todoLists.add(todoList);
        }

        return todoLists;
    }
}
