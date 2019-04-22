package com.list.todo.util;

import com.list.todo.entity.*;
import com.list.todo.payload.UserStatistics;
import com.list.todo.payload.UserSummary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectsProvider {

    private static final Long USER_ID = 1L;

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

    public static UserSummary createUserSummary(int postfixNumber) {
        return new UserSummary(
                "username" + postfixNumber,
                "name" + postfixNumber,
                "email@example.ua" + postfixNumber,
                "gravatarUrl" + postfixNumber);
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

    public static UserStatistics createUserStatistics() {
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setTodoListsNumber(21L);
        userStatistics.setTasksNumber(11L);
        userStatistics.setCompletedTasksNumber(5L);
        userStatistics.setFollowedUsersNumber(2);
        userStatistics.setFollowersNumber(3);

        return userStatistics;
    }

    public static User createUser(int postfixNumber) {
        return new User(
                "name" + postfixNumber,
                "username" + postfixNumber,
                "email@example.ua" + postfixNumber,
                "password" + postfixNumber,
                "gravatarHash" + postfixNumber);
    }

    public static List<Share> createListOfShares() {
        Share share = new Share();
        share.setSharedTodoList(createTodoList());

        List<Share> shareList = new ArrayList<>();
        shareList.add(share);

        return shareList;
    }

    public static List<Follower> createListOfFollowers() {
        return new ArrayList<Follower>(2) {{
            add(new Follower(USER_ID, new User()));
            add(new Follower(USER_ID, new User()));
        }};


    }

    public static List<UserSummary> createListOfUserSummaries() {
        return new ArrayList<UserSummary>(2) {{
            add(new UserSummary());
            add(new UserSummary());
        }};
    }
}
