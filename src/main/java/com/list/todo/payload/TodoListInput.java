package com.list.todo.payload;

import com.list.todo.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoListInput {
    private String todoListName;
    private Set<Task> tasks;
}
