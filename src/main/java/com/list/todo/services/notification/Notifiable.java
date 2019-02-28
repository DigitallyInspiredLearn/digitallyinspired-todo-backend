package com.list.todo.services.notification;

import com.list.todo.entity.TodoList;
import com.list.todo.entity.User;

public interface Notifiable {

    void notifyAboutSharingTodoList(User ownerUser, User targetUser, TodoList sharedTodoList);

    void notifyAboutAddingTodoList(User ownerUser, TodoList todoList);

    void notifyAboutUpdatingTodoList(User ownerUser, TodoList todoList);

    void notifyAboutDeletingTodoList(User ownerUser, TodoList todoList);
}
