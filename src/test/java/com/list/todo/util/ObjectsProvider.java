package com.list.todo.util;

import com.list.todo.entity.*;
import com.list.todo.payload.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

public class ObjectsProvider {

    public static Task createTask() {
        return Task.builder()
                .body("task")
                .isComplete(false)
                .priority(Priority.NOT_SPECIFIED)
                .build();
    }

    public static Task createTask(String createdBy, TodoList todoList, Long taskId) {
        Task task = Task.builder()
                .body("task")
                .todoList(todoList)
                .createdBy(createdBy)
                .build();
        task.setId(taskId);

        return task;
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

    public static Set<Task> createSetOfTasks(String currentUser) {
        Task task1 = Task.builder()
                .body("gg")
                .isComplete(false)
                .createdBy(currentUser)
                .build();
        task1.setId(3L);

        return new HashSet<Task>() {{
            add(task1);
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

    public static TodoList createTodoList(String createdBy) {
        return TodoList.builder()
                .todoListName("ff")
                .createdBy(createdBy)
                .build();
    }

    public static TodoList createTodoListWithTasks(String createdBy, Set<Task> tasks) {
        return TodoList.builder()
                .todoListName("ff")
                .createdBy(createdBy)
                .tasks(tasks)
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

    public static List<TodoList> createListOfTodoLists(String currentUser) {

        Task task1 = createTask(currentUser, null, 3L);

        Task task2 = createTask(currentUser, null, 4L);

        Set<Task> tasks = new HashSet<Task>() {{
            add(task1);
            add(task2);
        }};

        TodoList todoList1 = createTodoListWithTasks(currentUser, tasks);
        todoList1.setId(1L);

        TodoList todoList2 = createTodoListWithTasks(currentUser, tasks);
        todoList2.setId(5L);

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

    public static Tag createTag(Long userId, Long tagId) {
        Tag tag = new Tag("Home", userId, "ff");
        tag.setId(tagId);

        return tag;
    }

    public static List<Tag> createListOfTags(Long ownerId) {
        Long tagId = 2L;
        String nameTag = "Home";

        Tag tag = new Tag(nameTag, ownerId, "fg");
        tag.setId(tagId);

        Long tag2Id = 3L;
        String name2Tag = "Home";

        Tag tag2 = new Tag(name2Tag, ownerId, "ff");
        tag2.setId(tag2Id);

        return new ArrayList<Tag>() {{
            add(tag);
            add(tag2);
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

    public static User createUserWithUserSettings(int postfixNumber, UserSettings userSettings) {
        return User.builder()
                .name("name" + postfixNumber)
                .username("username" + postfixNumber)
                .email("email" + postfixNumber)
                .password("password" + postfixNumber)
                .userSettings(userSettings)
                .gravatarHash("gravatarHash" + postfixNumber)
                .build();
    }

    public static TagInput getTagInput() {
        return new TagInput("Job", "ff");
    }

    public static Set<TagTaskKey> createListOfTaggedTask(Long ownerId) {
        Long tagId = 6L;
        String nameTag = "";

        Tag tag = new Tag(nameTag, ownerId, "");
        tag.setId(tagId);

        Long taskId = 3L;

        TagTaskKey tagTaskKey = new TagTaskKey(taskId, tag);
        tagTaskKey.setId(7L);

        return new HashSet<TagTaskKey>() {{
            add(tagTaskKey);
        }};
    }

    public static User createUser(UserInput userInput) {
        return User.builder()
                .name(userInput.getUsername())
                .username(userInput.getUsername())
                .email(userInput.getEmail())
                .password(new BCryptPasswordEncoder().encode(userInput.getPassword()))
                .gravatarHash("ffg")
                .role(RoleName.ROLE_USER)
                .build();
    }

    public static List<User> createListOfFollowers(UserSettings userSettings) {
        User user1 = createUserWithUserSettings(1, userSettings);
        User user2 = createUserWithUserSettings(2, userSettings);

        return Arrays.asList(user1, user2);
    }
}
