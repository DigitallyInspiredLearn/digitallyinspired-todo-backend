package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputTask {
    private String body;
    private Boolean isComplete;
    private Long todoListId;
}
