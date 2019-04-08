package com.list.todo.payload;

import com.list.todo.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoListInput {
    private String todoListName;
    private String comment;
    private Set<Task> tasks = new LinkedHashSet<>();
}
