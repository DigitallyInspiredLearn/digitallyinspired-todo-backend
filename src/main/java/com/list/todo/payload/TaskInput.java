package com.list.todo.payload;

import com.list.todo.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInput {
    private String body;
    private Boolean isComplete;
    private Long durationTime;
    private Priority priority;
    private Long todoListId;
}
